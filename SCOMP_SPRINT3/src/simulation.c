#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <pthread.h>
#include <semaphore.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <math.h>
#include <errno.h>
#include <limits.h>

#include "controller.h"
#include "flight.h"
#include "common.h"
#include "reportGeneration.h"

/*
  THREADS (violation + report)
  Componente - 	Tipo       - 	Função
  sim_mutex  -	mutex	   - proteger SHM entre threads
  sim_cond   - 	condition  - variable acordar report_thread

  PROCESSOS (flights)
  Componente - 	Tipo	  - Função
  mutex_sem	 - semáforo	  - proteger SHM entre processos
  step_sem	 - semáforo	  - controlar avanço do tempo
  done_sem	 - semáforo	  - confirmar fim de step
*/

/* ===================================================== */

static volatile sig_atomic_t running = 1;
static pthread_mutex_t sim_mutex;
static pthread_cond_t  sim_cond;

/* ===================================================== */
static int safe_sem_wait(sem_t *sem){
    while (sem_wait(sem) == -1) {
        if (errno != EINTR) return -1;
        if (!running) return -1;
    }
    return 0;
}

/* ===================================================== */
void handle_SIGINT(int sig){
    (void)sig;
    running = 0;
}

/* ===================================================== */
/* THREAD 1 - VIOLATION DETECTOR                         */
/* ===================================================== */
void *violation_thread(void *arg){

    //apontador para a memória partilhada
    SimulationSharedState *shm = (SimulationSharedState *)arg;

    //Abre o semáforo que protege a memória partilhada
    sem_t *mutex_sem = sem_open(SEM_MUTEX, 0);
    if (mutex_sem == SEM_FAILED) {
        perror("[VIOLATION] sem_open failed");
        return NULL;
    }

    int last_violation_pos_index[MAX_FLIGHTS];
    for (int i = 0; i < MAX_FLIGHTS; i++)
        last_violation_pos_index[i] = -1;

    while (running) {

        //Lock para ler a SHM
        //Ninguém pode modificar a SHM enquanto estamos a copiar
        if (safe_sem_wait(mutex_sem) == -1) break;

        //copiar tudo para nao manter a memoria bloqueada muito tempo
        int active_flights[MAX_FLIGHTS];
        FlightPositionShared local_positions[MAX_FLIGHTS];
        int local_history_counts[MAX_FLIGHTS];

        memcpy(active_flights, shm->active, sizeof(shm->active));
        memcpy(local_positions, shm->positions, sizeof(shm->positions));

        for (int i = 0; i < MAX_FLIGHTS; i++)
            local_history_counts[i] = shm->histories[i].count;

        //unlock do semaforo
        sem_post(mutex_sem);

        /* DETEÇÃO FORA DE LOCK */
        for (int a = 0; a < MAX_FLIGHTS; a++) {
            if (!active_flights[a]) continue;

            for (int b = a + 1; b < MAX_FLIGHTS; b++) {
                if (!active_flights[b]) continue;

                //distancia horizontal
                double hdist = hypot(local_positions[a].x - local_positions[b].x,
                                     local_positions[a].y - local_positions[b].y);
                //distancia vertical
                double vdist = fabs(local_positions[a].altitude - local_positions[b].altitude);
                //regra de separação
                int is_violating =
                    (hdist < shm->rules.horizontal_separation &&
                     vdist < shm->rules.vertical_separation);

                if (is_violating) {
                    //lock novamente pq vamos modificar o histories
                    if (safe_sem_wait(mutex_sem) != -1) {

                        int pos_idx_a = local_history_counts[a] - 1;
                        if (pos_idx_a >= 0 && pos_idx_a != last_violation_pos_index[a]) {
                            shm->histories[a].violation_count++;
                            last_violation_pos_index[a] = pos_idx_a;
                        }

                        int pos_idx_b = local_history_counts[b] - 1;
                        if (pos_idx_b >= 0 && pos_idx_b != last_violation_pos_index[b]) {
                            shm->histories[b].violation_count++;
                            last_violation_pos_index[b] = pos_idx_b;
                        }

                        //unlock
                        sem_post(mutex_sem);
                    }
                }
                //este mutex nao protege a shm
                //protege threads dentro do MESMO processo
                //protege global_vio_count, violations, num_violations etc
                //que sao usados pelas 2 threads
                //sem isso podiamos escrever violacoes e o report limpar
                pthread_mutex_lock(&sim_mutex);

                if (is_violating && !shm->violation_active[a][b]) {

                    shm->violation_active[a][b] = 1;

                    if (shm->global_violation_count < INT_MAX)
                        shm->global_violation_count++;

                    SafetyViolationEvent ev_data;
                    ev_data.global_id = shm->global_violation_count;
                    ev_data.flightA = local_positions[a].flightId;
                    ev_data.posA = local_positions[a];
                    ev_data.flightB = local_positions[b].flightId;
                    ev_data.posB = local_positions[b];
                    ev_data.horizontal_distance = hdist;
                    ev_data.vertical_distance = vdist;
                    ev_data.timestamp_sec = time(NULL);

                    if (shm->global_violation_count <= MAX_TOTAL_VIOLATIONS)
                        shm->all_violations[shm->global_violation_count - 1] = ev_data;

                    if (shm->num_violations < MAX_VIOLATIONS) {
                        shm->violations[shm->num_violations++] = ev_data;
                        shm->new_violation_flag = 1;
                        //acorda a report thread
                        pthread_cond_signal(&sim_cond);
                    }
                }
                else if (!is_violating) {
                    shm->violation_active[a][b] = 0;
                }

                //unlock final
                pthread_mutex_unlock(&sim_mutex);
            }
        }

        usleep(100000); // 100ms
    }

    //libertar a shm
    sem_close(mutex_sem);
    return NULL;
}

/* ===================================================== */
/* THREAD 2 - REPORT                                     */
/* ===================================================== */
/*Thread responsável por:
  receber violações
  imprimir estado
  escrever logs*/
void *report_thread(void *arg)
{
    //apontador shm
    SimulationSharedState *shm = (SimulationSharedState *)arg;

    // US105: Abre o semáforo para acesso à SHM (posições)
    sem_t *mutex_sem = sem_open(SEM_MUTEX, 0);
    if (mutex_sem == SEM_FAILED) {
        perror("[REPORT] sem_open failed");
        return NULL;
    }

    while (running) {

        /* US107: proteger acesso à memória partilhada de controle (flags e cond var) */
        pthread_mutex_lock(&sim_mutex);

        /* US107: esperar até existir pelo menos uma violação
        * ou até a simulação terminar (running == 0).
        */
        while (shm->new_violation_flag == 0 && running) {
            pthread_cond_wait(&sim_cond, &sim_mutex);
            /*como funciona:
              report thread fica bloqueada
              violation thread faz:
              shm->new_violation_flag = 1
              pthread_cond_signal(&sim_cond)
              acorda report thread*/
        }
        //se simulação acabou e não há trabalho
        //sai da thread
        if (!running && shm->new_violation_flag == 0) {
            pthread_mutex_unlock(&sim_mutex);
            break;
        }
        //guarda step atual
        int local_step = shm->current_step;
        //evita trabalhar diretamente na SHM
        int local_num = 0;
        SafetyViolationEvent local[MAX_VIOLATIONS];

        //Copiar violações da SHM
        if (shm->new_violation_flag) {

            local_num = shm->num_violations;
            //copia tudo para memória local
            //evita lock durante processamento
            //evita race conditions durante logging
            memcpy(local,
                   shm->violations,
                   local_num * sizeof(SafetyViolationEvent));

            //Reset do buffer
            shm->new_violation_flag = 0;

            /* RESET CORRETO DO BUFFER */
            shm->num_violations = 0;
        }
        //unlock do mutex das threads - violation thread pode continuar
        pthread_mutex_unlock(&sim_mutex);

        if (local_num > 0) {

            printf("[REPORT] Step=%d Active flights:\n", local_step);
            //Lock da SHM - ler SHM em segurança
            if (safe_sem_wait(mutex_sem) != -1) {
                for (int i = 0; i < MAX_FLIGHTS; i++) {
                    if (shm->active[i])
                        printf("Flight %d\n", shm->positions[i].flightId);
                }
                // unlock SHM
                sem_post(mutex_sem);
            }

            printf("[REPORT] Processing %d new violations! Writing to log...\n", local_num);
            log_live_violations(local_step, local, local_num);
        }
    }
    //fecha o semáforo neste processo
    sem_close(mutex_sem);
    return NULL;
}

/* ===================================================== */
/* CLEANUP                                               */
/* ===================================================== */
void cleanup_semaphores(sem_t *m, sem_t *s, sem_t *d){
    if (m != SEM_FAILED) {
        //fecha no processo
        sem_close(m);
        //apaga no sistema
        sem_unlink(SEM_MUTEX);
    }
    if (s != SEM_FAILED) {
        sem_close(s);
        sem_unlink(SEM_STEP);
    }
    /* US108: limpar semáforo de sincronização de step */
    if (d != SEM_FAILED) {
        sem_close(d);
        sem_unlink(SEM_DONE);
    }
}

void cleanup_shm(SimulationSharedState *shm, int fd){
    /* Desfaz o mapeamento da memória partilhada */
    munmap(shm, sizeof(*shm));
     /* Fecha o descritor de arquivo da memória partilhada */
    close(fd);
    /* Remove o objeto de memória partilhada do sistema */
    shm_unlink(SHM_NAME);
}

/* ===================================================== */
/* MAIN                                                  */
/* ===================================================== */
void start_simulation(SimulationConfig config, SafetyRules rules, const FlightPlan plans[]) {
    /* ---------------- INITIALIZATION ---------------- */
    // Limpar o log da última simulação
    clear_violations_log();

    /* ---------------- VALIDATION ---------------- */
    // Validação inicial de parâmetros e configuração para evitar iniciar a simulação com dados inválidos
    if (!validate_rules(rules)) return;
    if (!validate_config(config)) return;

    for (int i = 0; i < config.numFlights; i++) {
        if (!validate_areaBounds(&plans[i], config)) return;
        if (!validate_plan(&plans[i])) return;
    }

    /* ---------------- SIGNAL ---------------- */
    struct sigaction sa;
    /* US105: Configurar handler para SIGINT para permitir shutdown a partir do processo principal */
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = handle_SIGINT;
    /* US105: Não bloquea nenhum sinal adicional durante a execução do handler, para garantir que o processo possa responder rapidamente a múltiplos sinais de interrupção. */
    sigemptyset(&sa.sa_mask);
    //quando alguém fizer Ctrl+C, executa o nosso handler
    if (sigaction(SIGINT, &sa, NULL) == -1) {
        perror("sigaction");
        return;
    }

    /* ----------- INITIALIZE SHARED MEMORY ------------ */
    /* Variáveis para controle da memória partilhada */
    int fd;
    SimulationSharedState *shm;
    /* Criação do objeto de memória partilhada */
    /* O_CREAT: cria a memória partilhada se não existir, O_RDWR: acesso de leitura/escrita */
    // O_EXCL - falha se já existir
    shm_unlink(SHM_NAME); // Garantir que não existe uma memória partilhada pré-existente com o mesmo nome
    sem_unlink(SEM_MUTEX); // Garantir que não existe um semáforo pré-existente com o mesmo nome
    sem_unlink(SEM_STEP);  // Garantir que não existe um semáforo
    sem_unlink(SEM_DONE);  // Garantir que não existe um semáforo de step pré-existente
    fd = shm_open(SHM_NAME, O_CREAT | O_EXCL | O_RDWR, 0666);
    if (fd == -1) {
        perror("shm_open");
        return;
    }

    /* Define o tamanho da memória partilhada para acomodar a estrutura SimulationSharedState */
    if (ftruncate(fd, sizeof(SimulationSharedState)) == -1) {
        perror("ftruncate");
        close(fd);
        return;
    }

    /* Mapeia a memória partilhada no espaço de endereçamento do processo */
    /* PROT_READ | PROT_WRITE: permite leitura e escrita, MAP_SHARED: mudanças são visíveis para outros processos que mapearem a mesma memória */
    shm = mmap(NULL, sizeof(SimulationSharedState), PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);

    /* Verifica se o mapeamento foi bem-sucedido */
    if (shm == MAP_FAILED) {
        perror("mmap");
        close(fd);
        return;
    }

    /* Inicializa o conteúdo da memória partilhada */
    memset(shm, 0, sizeof(*shm));
    /* US105: Inicialização explícita do estado de controle da simulação */
    shm->simulation_running = 1; //simulação ativa
    shm->current_step = 0; //tempo começa em 0
    shm->rules = rules; //copia regras de segurança

    // Inicializar posições de partida e histórico para evitar falsas violações em (0,0)
    for (int i = 0; i < config.numFlights; i++) {
        shm->positions[i].flightId = plans[i].id;
        shm->histories[i].flightId = plans[i].id;
        shm->histories[i].count = 0;
        shm->histories[i].violation_count = 0;
        if (plans[i].numSegments > 0) {
            shm->positions[i].x = plans[i].segments[0].start.latitude;
            shm->positions[i].y = plans[i].segments[0].start.longitude;
            shm->positions[i].altitude = plans[i].segments[0].start.altitude.value;
            shm->positions[i].vx = 0.0;
            shm->positions[i].vy = 0.0;
            shm->positions[i].vz = 0.0;
        }
    }

    /* ---------------- SEMAPHORES (IPC) ---------------- */
    sem_t *mutex_sem, *step_sem, *done_sem;
    //permite 1 acesso à memoria partilhada
    mutex_sem = sem_open(SEM_MUTEX, O_CREAT, 0666, 1);
    step_sem  = sem_open(SEM_STEP,  O_CREAT, 0666, 0);
    /* US108: semáforo para sincronização lockstep — cada voo sinaliza quando termina o step */
    done_sem  = sem_open(SEM_DONE,  O_CREAT, 0666, 0);

    //se falhar
    if (mutex_sem == SEM_FAILED || step_sem == SEM_FAILED || done_sem == SEM_FAILED) {
        perror("sem_open");

        /* Se apenas um dos semáforos falhar o outro é fechado para depois terminar o sistema.*/
        cleanup_semaphores(mutex_sem, step_sem, done_sem);

        /* Limpeza em caso de falha na criação dos semáforos */
        cleanup_shm(shm, fd);
        return;
    }

    /* ---------------- MUTEX (THREAD SAFETY) ---------------- */
    /* Inicializa o mutex e a variável de condição para sincronização entre threads */
    if (pthread_mutex_init(&sim_mutex, NULL) != 0) {
        perror("pthread_mutex_init");
        cleanup_semaphores(mutex_sem, step_sem, done_sem);
        cleanup_shm(shm, fd);
        return;
    }

    if (pthread_cond_init(&sim_cond, NULL) != 0) {
        perror("pthread_cond_init");
        pthread_mutex_destroy(&sim_mutex);
        cleanup_semaphores(mutex_sem, step_sem, done_sem);
        cleanup_shm(shm, fd);
        return;
    }

    printf("[SIM] US105 initialized (SHM + threads + semaphores)\n");

    /* ---------------- THREADS ---------------- */
    pthread_t vtid, rtid;

    //criar thread que deteta colisões
    if (pthread_create(&vtid, NULL, violation_thread, shm) != 0) {
        perror("[SIM] pthread_create (violation) failed");
        //cleanup de emergência
        pthread_mutex_destroy(&sim_mutex);
        pthread_cond_destroy(&sim_cond);
        cleanup_semaphores(mutex_sem, step_sem, done_sem);
        cleanup_shm(shm, fd);
        return;
    }
    //criar thread que imprime/loga violações
    if (pthread_create(&rtid, NULL, report_thread, shm) != 0) {
        perror("[SIM] pthread_create (report) failed");
        //cleanup de emergência
        // 1. sinalizar shutdown global das threads
        running = 0;

        // 2. acordar thread bloqueada em pthread_cond_wait
        pthread_mutex_lock(&sim_mutex);
        //acorda TODAS as threads que estejam a dormir nesta condition variable
        pthread_cond_broadcast(&sim_cond);
        pthread_mutex_unlock(&sim_mutex);

        // 3. acordar threads presas em semáforos
        for (int i = 0; i < config.numFlights; i++) {
            sem_post(step_sem);
        }

        // 4. esperar thread de violação terminar naturalmente
        pthread_join(vtid, NULL);

        // 5. limpeza
        pthread_mutex_destroy(&sim_mutex);
        pthread_cond_destroy(&sim_cond);
        cleanup_semaphores(mutex_sem, step_sem, done_sem);
        cleanup_shm(shm, fd);

        return;
    }

    printf("[SIM] Threads created \n");

    /* ---------------- FORK FLIGHT PROCESSES ---------------- */
    pid_t pids[MAX_FLIGHTS];

    for (int i = 0; i < config.numFlights; i++) {
        //cria processo filho para cada avião
        pids[i] = fork();

        if (pids[i] == -1) {
            perror("fork");
            // matar filhos já lançados
            for (int j = 0; j < i; j++) {
                kill(pids[j], SIGTERM);
                waitpid(pids[j], NULL, 0);
            }

            // 1. parar execução das threads
            running = 0;

            // 2. acordar thread de condição (report thread)
            pthread_mutex_lock(&sim_mutex);
            shm->simulation_running = 0;
            pthread_cond_broadcast(&sim_cond);
            pthread_mutex_unlock(&sim_mutex);

            // 3. acordar threads bloqueadas em semáforos
            for (int k = 0; k < MAX_FLIGHTS; k++) {
                sem_post(mutex_sem);
            }

            // 4. esperar threads terminarem naturalmente
            pthread_join(vtid, NULL);
            pthread_join(rtid, NULL);

            // destruir mutex e cond
            pthread_mutex_destroy(&sim_mutex);
            pthread_cond_destroy(&sim_cond);

            // semáforos
            cleanup_semaphores(mutex_sem, step_sem, done_sem);

            // shared memory
            cleanup_shm(shm, fd);
            return;
        }

        if (pids[i] == 0) {
            // processo filho - executa o voo
            printf("[FLIGHT %d] PID=%d started\n", plans[i].id, getpid());
            run_flight(&plans[i], shm, i);
            exit(0);
        }
    }

    /* ============================
       US108 — Esperar que todos os flights fiquem ativos
       ============================ */
    int all_ready = 0;
    while (!all_ready) {
        all_ready = 1;
        //entra em zona crítica
        //bloqueia acesso à memória partilhada
        // porque shm->active[] é partilhado entre processos
        if (safe_sem_wait(mutex_sem) != -1) {
            //verifica todos os voos
            for (int i = 0; i < config.numFlights; i++) {
                if (shm->active[i] == 0) {
                    all_ready = 0;
                    break;
                }
            }
            //sai da zona crítica
            //desbloqueia a memória partilhada
            sem_post(mutex_sem);
        }
        //se ainda não está tudo pronto → espera um pouco
        if (!all_ready)
            usleep(1000); // 1ms
    }

    printf("[SIM] All flights initialized. Starting simulation...\n");


    // Flag local para controlo do loop e indicador de paragem antecipada (early stop)
    int early_stop = 0;

    printf("[SIM] US105 running\n");

    /* ---------------- MAIN LOOP ---------------- */
    while (running && shm->current_step < config.endTime) {

        // 1. Controlar a velocidade da simulação
        double sleep_time = (config.updateInterval / config.simulationSpeed) * 1000000.0;
        usleep((useconds_t)sleep_time);

        // 2. Incrementar o tempo da simulação com proteção do Mutex
        //proteger estado partilhado entre threads.
        pthread_mutex_lock(&sim_mutex);
        shm->current_step += (int)config.updateInterval;
        printf("[SIM] Step %d\n", shm->current_step);

        // 3. Verificar se o limite acumulado de violações de segurança foi atingido
        if (shm->global_violation_count >= rules.violation_threshold) {
            printf("[SIM] Violation threshold reached (%d). Stopping simulation.\n", shm->global_violation_count);
            running = 0;
            early_stop = 1;
        }

        // 4. Verificar se o tempo final da simulação foi atingido
        if (shm->current_step >= config.endTime) {
            running = 0;
            early_stop = 1;
        }
        //sair zona critica
        pthread_mutex_unlock(&sim_mutex);

        // 5. Verificar se ainda há voos ativos na Memória Partilhada
        int any_active = 0;
        if (safe_sem_wait(mutex_sem) != -1) {
            for (int i = 0; i < config.numFlights; i++) {
                if (shm->active[i]) {
                    any_active = 1;
                    break;
                }
            }
            sem_post(mutex_sem);
        } else {
            // Se o sem_wait falhar por interrupção, assumimos ativo para evitar saídas abruptas
            any_active = 1;
        }

        // Se todos os voos já aterraram/terminaram, saímos do loop
        if (!any_active) {
            printf("[SIM] All flights finished. Stopping simulation.\n");
            running = 0;
            early_stop = 1;
            break;
        }

        // 6. US108: Barreira de Sincronização — dar permissão a cada voo ativo para avançar o step
        int active_count = 0; // vai contar quantos voos ainda estão ativos neste step
        //Entrar na zona crítica (SHM)
        //bloqueia acesso à memória partilhada
        //porque vamos ler:
        //shm->active[i]
        if (safe_sem_wait(mutex_sem) == -1) break;

        for (int i = 0; i < config.numFlights; i++) {
            if (shm->active[i]) {
                //Libertar o voo para avançar para proximo step
                sem_post(step_sem); // Desbloqueia o semáforo de passo do voo i
                active_count++;
            }
        }
        //sair zona critica - liberta a SHM
        sem_post(mutex_sem);

        // 7. US108: Esperar que todos os voos ativos sinalizem o fim do step (lockstep)
        for (int i = 0; i < active_count; i++) {
            if (safe_sem_wait(done_sem) == -1) break;
        }
    }

    /* ---------------- SHUTDOWN ---------------- */
    printf("[SIM] Shutting down...\n");

    // 1. Sinalizar às threads (violation e report) que a simulação parou
    //entra no mutex para proteger variáveis partilhadas entre threads
    pthread_mutex_lock(&sim_mutex);
    shm->simulation_running = 0;
    //acorda TODAS as threads que estejam bloqueadas em cond_wait
    //sem isto, elas poderiam ficar presas para sempre
    pthread_cond_broadcast(&sim_cond);
    //liberta o mutex
    pthread_mutex_unlock(&sim_mutex);

    // 2. Destrancar quaisquer voos que possam estar bloqueados no semáforo de passo
    if (step_sem != SEM_FAILED) {
        for (int i = 0; i < config.numFlights; i++) {
            //“acorda” todos os voos que estão bloqueados em:
            //sem_wait(step_sem)
            sem_post(step_sem);
        }
    }

    // 3. Forçar a paragem com SIGTERM de qualquer voo que ainda esteja ativo (por segurança)
    for (int i = 0; i < config.numFlights; i++) {
        if (pids[i] > 0) {
            kill(pids[i], SIGTERM);
        }
    }

    // 4. Esperar por todos os processos filhos (recolher status para evitar zombies)
    printf("[SIM] Waiting for all flights...\n");
    for (int i = 0; i < config.numFlights; i++) {
        if (pids[i] > 0) {
            int status;
            pid_t pid;
            // O loop do waitpid lida com interrupções por sinal (EINTR)
            while ((pid = waitpid(pids[i], &status, 0)) == -1 && errno == EINTR);

            if (pid != -1) {
                //processo terminou normalmente
                if (WIFEXITED(status)) {
                    printf("[SIM] Flight %d exited (code=%d)\n", plans[i].id, WEXITSTATUS(status));
                //processo morreu por sinal (ex: SIGTERM)
                } else if (WIFSIGNALED(status)) {
                    printf("[SIM] Flight %d killed by signal %d\n", plans[i].id, WTERMSIG(status));
                }
            }
        }
    }

    // 5. Esperar que as threads terminem a sua execução
    /*garante que:
    violation_thread acabou
    report_thread acabou
    sem isto:
    pode haver acesso à memória já libertada*/
    pthread_join(vtid, NULL);
    pthread_join(rtid, NULL);

    // 6. Gerar o Relatório Final da Simulação (US109)
    printf("[SIM] Generating final report...\n");
    //proteger leitura da SHM - garante consistência dos dados
    pthread_mutex_lock(&sim_mutex);
    // Calcula o número total de violações ocorridas vs. capacidade do buffer
    int total_stored = (shm->global_violation_count < MAX_TOTAL_VIOLATIONS) ?
                        shm->global_violation_count : MAX_TOTAL_VIOLATIONS;
    int final_total = shm->global_violation_count;
    //liberta mutex
    pthread_mutex_unlock(&sim_mutex);

    generate_report(shm->histories, config.numFlights, shm->all_violations,
                    total_stored, rules.violation_threshold, final_total, early_stop);


    /* ----------------- CLEANUP FINAL ------------------ */
    // 7. Fechar os Semáforos e desvincular (unlink) do sistema operativo
    cleanup_semaphores(mutex_sem, step_sem, done_sem);

    // 8. Destruir os mecanismos de sincronização locais
    pthread_cond_destroy(&sim_cond);
    pthread_mutex_destroy(&sim_mutex);

    // 9. Libertar, fechar e desvincular a Memória Partilhada
    cleanup_shm(shm, fd);

    printf("[SIM] Simulation completed successfully\n");
}