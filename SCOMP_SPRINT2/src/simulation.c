#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <time.h>
#include <errno.h>
#include <math.h>
#include <sys/stat.h>

#include "controller.h"
#include "flight.h"
#include "common.h"
#include "util.h"

#define SIMULATION_LOG_FILE "log/simulation.txt"

// deve parar ou andar?
// 1 = continua a correr
// 0 = parar
static volatile sig_atomic_t running = 1;

// handler do CTRL+C (SIGINT)
void handle_SIGINT(int sig) {
    (void)sig;
    running = 0;
}

void start_simulation(SimulationConfig config, SafetyRules rules, const FlightPlan plans[]) {

    /* Validar regras de segurança */
    if (!validate_rules(rules)) {
        fprintf(stderr, "[ERROR] Invalid safety rules\n");
        generate_report_validation_failure("[ERROR] Invalid safety rules");
        return;
    }

    /* Validar configuração da simulação */
    if (!validate_config(config)) {
        fprintf(stderr, "[ERROR] Invalid simulation configuration\n");
        generate_report_validation_failure("[ERROR] Invalid simulation configuration");
        return;
    }

    for(int i = 0; i < config.numFlights; i++){
        if (!validate_areaBounds(&plans[i], config)) {
            fprintf(stderr, "[ERROR] Flight plans out of geographic bounds\n");
            generate_report_validation_failure("[ERROR] Flight plans out of geographic bounds");
            return;
        }
    }

    for(int i = 0; i < config.numFlights; i++){
        if (!validate_plan(&plans[i])) {
            fprintf(stderr, "[ERROR] Invalid flight plan for flight %d\n", plans[i].id);
            generate_report_validation_failure("[ERROR] Invalid flight plan");
            return;
        }
    }

    /* Validar combustível de cada voo */
    for (int i = 0; i < config.numFlights; i++) {
        if (!check_fuel(&plans[i])) {
            fprintf(stderr, "[ERROR] Flight %d has insufficient fuel for the planned route\n", plans[i].id);
            generate_report_validation_failure("[ERROR] Insufficient fuel for flight");
            return;
        }
    }

    /* Criar diretório de logs se não existir */
    mkdir("log", 0755);

    FILE *log = fopen(SIMULATION_LOG_FILE, "w");
    if (!log) {
        perror("[ERROR] The simulation log file cannot be created");
        return;
    }

    fprintf(log,
            "========= SIMULATION STARTED =========\n"
            "  Number of Flights = %d\n"
            "  Duration of Simulation=%d\n",
            config.numFlights,
            config.endTime - config.startTime);
    fflush(log);

    // ---------------- SIGNAL SETUP ----------------
    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = handle_SIGINT;
    sigemptyset(&sa.sa_mask);

    if (sigaction(SIGINT, &sa, NULL) == -1) {
        perror("[ERROR] sigaction failed");
        fprintf(log, "[SIMULATION ABORTED] sigaction setup failed!\n");
        fclose(log);
        return;
    }

    // ---------------- STRUCTURES ----------------

    // pipe de posições: flight -> controller
    // pipes[i][0] = leitura (controller)
    // pipes[i][1] = escrita (flight)
    int pipes[MAX_FLIGHTS][2];

    // pipe de ready: flight avisa controller que está pronto para o step
    // pipe_ready[i][0] = leitura (controller)
    // pipe_ready[i][1] = escrita (flight)
    int pipe_ready[MAX_FLIGHTS][2];

    pid_t        pids[MAX_FLIGHTS];
    FlightHistory history[MAX_FLIGHTS];
    int          flight_active[MAX_FLIGHTS];

    for (int i = 0; i < config.numFlights; i++) {
        history[i].count           = 0;
        history[i].violation_count = 0;
        flight_active[i]           = 1;
    }

    // ---------------- CREATE PROCESSES ----------------

    for (int i = 0; i < config.numFlights; i++) {

        if (pipe(pipes[i]) == -1 || pipe(pipe_ready[i]) == -1) {
            perror("[ERROR] pipe creation failed");
            fprintf(log, "[SIMULATION ABORTED] pipe failed!\n");
            fclose(log);
            return;
        }

        pid_t pid = fork();

        if (pid < 0) {
            perror("[ERROR] fork failed");
            fprintf(log, "[SIMULATION ABORTED] fork failed!\n");
            fclose(log);
            return;
        }

        if (pid == 0) {

            // filho fecha leitura dos pipes
            close(pipes[i][0]);
            close(pipe_ready[i][0]);

            printf("[FLIGHT %d] started (PID=%d)\n", config.flightIds[i], getpid());

            // corre o flight — envia ready antes de cada step, aguarda SIGUSR2
            run_flight(&plans[i], pipes[i][1], pipe_ready[i][1], (int)config.updateInterval);

            close(pipes[i][1]);
            close(pipe_ready[i][1]);

            printf("[FLIGHT %d] exiting normally\n", config.flightIds[i]);
            exit(0);
        }

        // pai guarda PID e fecha escrita dos pipes
        pids[i] = pid;
        close(pipes[i][1]);
        close(pipe_ready[i][1]);
    }

    printf("[SIM] Simulation started\n");

    time_t start          = time(NULL);
    int    duration       = config.endTime - config.startTime;
    int    threshold      = rules.violation_threshold;
    int    violation_count = 0;
    int    early_stop     = 0;
    int    active_flights = config.numFlights;
    int    current_step   = 0;

    SafetyViolationEvent events[threshold];

    // ---------------- MAIN LOOP ----------------

    while (running && active_flights > 0) {

        /* verificar tempo de simulação */
        if (time(NULL) - start >= duration) {
            printf("[SIM] Simulation time finished\n");
            fprintf(log, "\n[SIM] Simulation time finished\n");
            fflush(log);
            break;
        }

        printf("\n========== STEP %d ==========\n", current_step);

        // ------------------------------------------------
        // US103: ESPERAR QUE TODOS OS FLIGHTS ESTEJAM PRONTOS
        // cada flight escreve 1 byte no pipe_ready antes de entrar em pause()
        // só então o controller envia SIGUSR2
        // ------------------------------------------------
        for (int i = 0; i < config.numFlights; i++) {
            if (!flight_active[i])
                continue;

            char ready;
            ssize_t r = read(pipe_ready[i][0], &ready, 1);

            if (r == 0) {
                /* flight terminou */
                printf("[SIM] Flight %d finished\n", config.flightIds[i]);
                flight_active[i] = 0;
                active_flights--;
            } else if (r == -1 && errno != EINTR) {
                perror("[ERROR] read ready");
            }
        }

        /* se já não há flights ativos, sair */
        if (active_flights == 0)
            break;

        for (int i = 0; i < config.numFlights; i++) {
            if (flight_active[i]) {
                if (kill(pids[i], SIGUSR2) == -1)
                    perror("[SIM] kill SIGUSR2");
            }
        }

        for (int i = 0; i < config.numFlights; i++) {

            if (!flight_active[i])
                continue;

            FlightPosition pos;
            ssize_t n = read(pipes[i][0], &pos, sizeof(pos));

            if (n == 0) {
                printf("[SIM] Flight %d finished\n", config.flightIds[i]);
                flight_active[i] = 0;
                active_flights--;
                continue;
            }

            if (n == -1) {
                if (errno == EINTR) { i--; continue; }
                perror("[ERROR] read failed");
                continue;
            }

            if (n != sizeof(pos))
                continue;

            printf("[SIM][STEP %d] Flight %d -> (%.2f, %.2f) alt=%.2f\n",
                   current_step, pos.flightId, pos.x, pos.y, pos.altitude);

            if (history[i].count < MAX_HISTORY)
                history[i].positions[history[i].count++] = pos;
        }

        // ------------------------------------------------
        // CHECK VIOLATIONS (US102)
        // ------------------------------------------------
        if (capture_violation(log, config, rules, history, pids, flight_active, &violation_count, events)) {
            early_stop = 1;
            break;
        }

        current_step++;
    }

    if (!early_stop && running)
        fprintf(log, "[SIM] Simulation ended normally!\n");
    else if (!early_stop)
        fprintf(log, "[SIM] Simulation was ended abruptly!\n");

    fflush(log);

    // ---------------- TERMINATE CHILDREN ----------------
    printf("[SIM] Terminating all flights...\n");

    for (int i = 0; i < config.numFlights; i++) {
        close(pipe_ready[i][0]); /* fechar leitura do ready para desbloquear flights em write */
        if (kill(pids[i], SIGTERM) == -1)
            perror("[SIM] kill SIGTERM");
    }

    // ---------------- WAIT CHILDREN ----------------
    printf("[SIM] Waiting for all flights...\n");

    for (int i = 0; i < config.numFlights; i++) {

        int   status;
        pid_t pid;

        while ((pid = waitpid(pids[i], &status, 0)) == -1) {
            if (errno == EINTR) continue;
            perror("[ERROR] waitpid failed");
            break;
        }

        if (pid == -1) continue;

        if (WIFEXITED(status))
            printf("[SIM] Flight %d exited (code=%d)\n", config.flightIds[i], WEXITSTATUS(status));
        else if (WIFSIGNALED(status))
            printf("[SIM] Flight %d killed by signal %d\n", config.flightIds[i], WTERMSIG(status));
    }

    // ---------------- CLEANUP ----------------
    for (int i = 0; i < config.numFlights; i++)
        close(pipes[i][0]);

    fprintf(log,
            "[SIM] Simulation shutdown complete\n"
            " Total of Violations = %d\n",
            violation_count);
    fflush(log);
    fclose(log);

    /* gerar relatório */
    printf("Generating report...\n");
    int result = generate_report(history, config.numFlights, events, threshold, violation_count, early_stop);

    if (result == -1) {
        fprintf(stderr, "[ERROR] Report generation failed\n");
        return;
    }
    printf("Report generated successfully!\n");

    // ---------------- PRINT HISTORY ----------------
    printf("\n===== FLIGHT HISTORY =====\n");

    for (int i = 0; i < config.numFlights; i++) {
        printf("Flight %d:\n", config.flightIds[i]);
        for (int j = 0; j < history[i].count; j++) {
            printf("  (%.2f, %.2f) => %.2f\n",
                   history[i].positions[j].x,
                   history[i].positions[j].y,
                   history[i].positions[j].altitude);
        }
    }

    for (int i = 0; i < violation_count; i++) {
        char time[6];
        turn_time_into_char(events[i].timestamp_sec / 60, time, sizeof(time));
        printf("\n[VIOLATION #%d] Time=%s\n Horizontal Distance=%.4f Vertical Distance=%.4f\n",
               i + 1, time,
               events[i].horizontal_distance,
               events[i].vertical_distance);
        printf("   Flight %d : (%.2f, %.2f) on %.2f at %ld\n"
               "   Flight %d : (%.2f, %.2f) on %.2f at %ld\n",
               events[i].flightA,
               events[i].posA.x, events[i].posA.y, events[i].posA.altitude,
               events[i].timestamp_sec,
               events[i].flightB,
               events[i].posB.x, events[i].posB.y, events[i].posB.altitude,
               events[i].timestamp_sec);
    }

    printf("[SIM] Simulation shutdown complete\n");
}