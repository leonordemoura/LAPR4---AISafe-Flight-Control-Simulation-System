#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <time.h>
#include <errno.h>
#include <math.h>
#include "common.h"
#include "flight.h"

//Indica se o processo do voo continua a executar.
static volatile sig_atomic_t running = 1;

//Função auxiliar para esperar num semáforo
static int safe_sem_wait(sem_t *sem)
{
    //Tenta bloquear no semáforo
    while (sem_wait(sem) == -1) {
        //Se o erro não foi causado por um sinal
        if (errno != EINTR) return -1;
        //Se foi interrompido por sinal
        if (!running) return -1;
    }
    return 0;
}

//Quando o controller faz:
//kill(pid, SIGTERM);
//o processo entra aqui.
void handle_SIGTERM(int sig)
{
    (void)sig;
    running = 0;
}

//mutex_sem semaforo = proteção da memória partilhada
//apenas 1 processo/thread pode mexer na SHM ao mesmo tempo

//sim_mutex (pthread mutex)
//protege entre threads - Evita race conditions entre threads do mesmo processo.
// ex do que protege : shm->violations[], shm->num_violations, shm->global_violation_count

//step_sem (barreira de sincronização)


int run_flight(const FlightPlan *plan, SimulationSharedState *shm, int index)
{
    //Estrutura usada para configurar sinais.
    struct sigaction sa;
    //Inicializa tudo a zero
    memset(&sa, 0, sizeof(sa));

    /* US105: Configurar handler para SIGTERM e SIGINT para permitir shutdown gracioso */
    sa.sa_handler = handle_SIGTERM;
    sigaction(SIGTERM, &sa, NULL);
    sigaction(SIGINT,  &sa, NULL);

    /* US108 — Marcar voo como ativo imediatamente */
    shm->active[index] = 1;
    //Guarda o ID do voo.
    shm->positions[index].flightId = plan->id;

    /* Abrir semáforos
    Semáforo de exclusão mútua.
    Protege a SHM.*/
    sem_t *mutex_sem = sem_open(SEM_MUTEX, 0);
    /*Semáforo usado pelo controller para dizer
    podes avançar um step*/
    sem_t *step_sem  = sem_open(SEM_STEP, 0);
    /*Semáforo usado pelo voo para responder:
    já terminei este step*/
    sem_t *done_sem  = sem_open(SEM_DONE, 0);

    //Verificar erros
    if (mutex_sem == SEM_FAILED || step_sem == SEM_FAILED || done_sem == SEM_FAILED) {
        perror("[FLIGHT] sem_open failed");
        if (mutex_sem != SEM_FAILED) sem_close(mutex_sem);
        if (step_sem  != SEM_FAILED) sem_close(step_sem);
        if (done_sem  != SEM_FAILED) sem_close(done_sem);
        return -1;
    }

    //Serve para saber se o voo foi interrompido.
    int interrupted = 0;
    //Loop dos segmentos
    for (int s = 0; s < plan->numSegments && running && shm->simulation_running; s++) {

        FlightSegment seg = plan->segments[s];
        //Calcular deslocamentos
        double dx   = seg.end.latitude        - seg.start.latitude;
        double dy   = seg.end.longitude       - seg.start.longitude;
        double dalt = seg.end.altitude.value  - seg.start.altitude.value;
        //Segmento dividido em
        int steps_in_segment = 2;

        for (int k = 0; k <= steps_in_segment && running && shm->simulation_running; k++) {

            /* US108: Esperar permissão do controller para avançar step */
            if (safe_sem_wait(step_sem) == -1) {
                interrupted = 1;
                break;
            }

            double t = (double)k / steps_in_segment;

            FlightPositionShared pos;
            pos.flightId = plan->id;
            pos.x        = seg.start.latitude       + t * dx;
            pos.y        = seg.start.longitude      + t * dy;
            pos.altitude = seg.start.altitude.value + t * dalt;
            pos.vx       = dx   / steps_in_segment;
            pos.vy       = dy   / steps_in_segment;
            pos.vz       = dalt / steps_in_segment;

            //Entrar na zona crítica
            /* Atualizar memória partilhada */
            // LOCK.
            // Ninguém pode alterar a SHM ao mesmo tempo.
            if (safe_sem_wait(mutex_sem) == -1) {
                interrupted = 1;
                break;
            }
            //Verificar se a simulação acabou
            if (!shm->simulation_running) {
                //Se terminou - unlock
                sem_post(mutex_sem);
                interrupted = 1;
                break;
            }

            //Atualizar posição atual
            shm->positions[index] = pos;

            /* US109: Registar histórico de trajetória */
            if (shm->histories[index].count < MAX_HISTORY) {
                shm->histories[index].positions[shm->histories[index].count++] = pos;
            }

            //Libertar semáforo - unlock
            sem_post(mutex_sem);

            /* US108: sinaliza fim do step ao controller (lockstep) */
            printf("[FLIGHT %d] seg=%d step=%d pos=(%.4f, %.4f, %.2f)\n",
                   plan->id, s, k, pos.x, pos.y, pos.altitude);
            sem_post(done_sem);
        }

        if (interrupted) break;
    }

    /* Limpar estado de ativo */
    // Marcar como inativo - lock
    if (safe_sem_wait(mutex_sem) != -1) {
        //O voo desaparece da lista de voos ativos.
        shm->active[index] = 0;
        //UNLOCK
        sem_post(mutex_sem);
    }

    //Fechar semáforos
    sem_close(mutex_sem);
    sem_close(step_sem);
    sem_close(done_sem);

    if (interrupted) {
        return -1;
    }

    printf("[FLIGHT %d] finished\n", plan->id);
    return 0;
}