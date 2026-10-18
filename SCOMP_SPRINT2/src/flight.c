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
#include "util.h"

#define CRUISE_SPEED_DEG_PER_MIN 0.1

// deve parar ou andar?
// 0 = continua
// 1 = parar (SIGTERM recebido)
static volatile sig_atomic_t g_stop = 0;

// autorização do controller para avançar para o próximo step
// 0 = aguarda
// 1 = autorizado (SIGUSR2 recebido)
static volatile sig_atomic_t g_next_step = 0;

// handler SIGUSR1: violação de separação detectada pelo controller
void handle_SIGUSR1(int sig) {
    (void) sig;
    static const char msg[] = "[FLIGHT] SIGUSR1: Separation violation\n";
    write(STDERR_FILENO, msg, sizeof(msg) - 1);
}

// handler SIGTERM: controller ordena terminação da simulação
void handler_SIGTERM(int sig) {
    (void)sig;
    g_stop = 1;
    g_next_step = 1;   // desbloqueia o sigsuspend
}

// handler SIGUSR2: controller autoriza o flight a avançar para o próximo step (US103)
void handler_SIGUSR2(int sig) {
    (void) sig;
    g_next_step = 1;
}

void run_flight(const FlightPlan *plan, int write_fd, int ready_fd, int update_interval) {

    if (!plan) {
        fprintf(stderr, "[FLIGHT] Invalid FlightPlan\n");
        close(write_fd);
        close(ready_fd);
        exit(1);
    }

    struct sigaction sa_usr, sa_term, sa_step;

    memset(&sa_usr,  0, sizeof(sa_usr));
    memset(&sa_term, 0, sizeof(sa_term));
    memset(&sa_step, 0, sizeof(sa_step));

    /* SIGUSR1: violação de separação */
    sa_usr.sa_handler = handle_SIGUSR1;
    /* Bloquear todos os sinais enquanto o handler de SIGUSR1 estiver em execução para evitar reentrância */
    sigfillset(&sa_usr.sa_mask);

    /* Registar o handler para SIGUSR1 */
    if (sigaction(SIGUSR1, &sa_usr, NULL) == -1) {
        perror("sigaction SIGUSR1");
        close(write_fd);
        close(ready_fd);
        exit(1);
    }

    /* Atribuir o handler para SIGTERM */
    sa_term.sa_handler = handler_SIGTERM;
    /* Não boquea nenhum sinal durante a execução do handler de SIGTERM*/
    sigemptyset(&sa_term.sa_mask);

    /* Register SIGTERM handler */
    if (sigaction(SIGTERM, &sa_term, NULL) == -1) {
        perror("sigaction SIGTERM");
        close(write_fd);
        close(ready_fd);
        exit(1);
    }

    /* SIGUSR2: autorização do controller para avançar */
    sa_step.sa_handler = handler_SIGUSR2;
    sigemptyset(&sa_step.sa_mask);

    if (sigaction(SIGUSR2, &sa_step, NULL) == -1) {
        perror("sigaction SIGUSR2");
        close(write_fd);
        close(ready_fd);
        exit(1);
    }

    /* máscara para sigsuspend */
    sigset_t block_mask, old_mask;

    sigemptyset(&block_mask);
    sigaddset(&block_mask, SIGUSR2);

    /* bloquear SIGUSR2 para evitar race condition */
    if (sigprocmask(SIG_BLOCK, &block_mask, &old_mask) == -1) {
        perror("sigprocmask");
        close(write_fd);
        close(ready_fd);
        exit(1);
    }

    int total_steps   = 0;
    int departure_sec = convert_into_timestamp(plan->departureTime) * 60;

    for (int s = 0; s < plan->numSegments && !g_stop; s++) {

        FlightSegment seg = plan->segments[s];

        double dx   = seg.end.latitude  - seg.start.latitude;
        double dy   = seg.end.longitude - seg.start.longitude;
        double dalt = seg.end.altitude.value - seg.start.altitude.value;

        double dist2d = hypot(dx, dy);

        /* ignorar segmentos com distância nula */
        if (dist2d < 1e-9 && fabs(dalt) < 1e-3)
            continue;

        /* número de passos proporcional à distância e ao intervalo */
        double duration_min = dist2d / CRUISE_SPEED_DEG_PER_MIN;
        int steps = (int)(duration_min / update_interval);

        if (steps < 2)   steps = 2;
        if (steps > 500) steps = 500;

        for (int k = 0; k <= steps && !g_stop; k++) {

            /* avisa controller que está pronto */
            char ready = 1;

            if (write(ready_fd, &ready, 1) == -1) {
                if (errno == EPIPE || g_stop)
                    break;

                perror("[FLIGHT] write ready");
            }

            /* reset autorização */
            g_next_step = 0;
            while (!g_next_step && !g_stop) {
                sigsuspend(&old_mask);
            }

            if (g_stop)
                break;

            double t = (double)k / (double)steps;

            FlightPosition pos;

            pos.flightId  = plan->id;
            pos.x         = seg.start.latitude  + t * dx;
            pos.y         = seg.start.longitude + t * dy;
            pos.altitude  = seg.start.altitude.value + t * dalt;
            pos.timestamp = departure_sec + (total_steps + k) * update_interval * 60;

            /* envia posição ao controller */
            ssize_t n = write(write_fd, &pos, sizeof(pos));

            if (n == -1) {

                if (errno == EINTR)
                    continue;

                perror("[FLIGHT] write");

                close(write_fd);
                close(ready_fd);

                exit(1);
            }

            if (n != (ssize_t)sizeof(pos)) {

                fprintf(stderr, "[FLIGHT] partial write on flight %d\n", plan->id);

                close(write_fd);
                close(ready_fd);

                exit(1);
            }
        }

        total_steps += steps + 1;
    }

    sigprocmask(SIG_SETMASK, &old_mask, NULL);

    close(write_fd);
    close(ready_fd);

    exit(0);
}