#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <errno.h>
#include <math.h>

#include "../include/controller.h"
#include "../include/flight.h"
#include "../include/common.h"

/* cria um FlightPlan mínimo válido */
static FlightPlan make_plan() {
    FlightPlan p;
    memset(&p, 0, sizeof(p));
    p.id = 1;
    strcpy(p.departureTime, "00:00");
    p.numSegments = 1;
    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].start.altitude.value = 1000;
    p.segments[0].end.latitude = 1;
    p.segments[0].end.longitude = 1;
    p.segments[0].end.altitude.value = 1000;
    return p;
}

/* -----------------------------
   TEST 1: SIGTERM stops flight
------------------------------*/
void test_flight_sigterm() {

    printf("\nTESTE1\n");

    int pipefd[2];
    int ready_pipe[2];

    assert(pipe(pipefd) == 0);
    assert(pipe(ready_pipe) == 0);

    FlightPlan plan = make_plan();

    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {

        close(pipefd[0]);
        close(ready_pipe[0]);

        run_flight(&plan, pipefd[1], ready_pipe[1], 1);

        exit(0);
    }

    close(pipefd[1]);
    close(ready_pipe[1]);

    /* esperar READY do flight */
    char ready;
    assert(read(ready_pipe[0], &ready, 1) == 1);

    /* flight está bloqueado em sigsuspend */
    kill(pid, SIGTERM);

    int status;

    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] SIGTERM terminates blocked flight\n");
}
/* -----------------------------
   TEST 2: SIGUSR1 does not crash flight
------------------------------*/
void test_flight_sigusr1() {

    printf("\nTESTE3\n");

    int pipefd[2];
    int ready_pipe[2];

    assert(pipe(pipefd) == 0);
    assert(pipe(ready_pipe) == 0);

    FlightPlan plan = make_plan();

    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {

        close(pipefd[0]);
        close(ready_pipe[0]);

        run_flight(&plan, pipefd[1], ready_pipe[1], 1);

        exit(0);
    }

    close(pipefd[1]);
    close(ready_pipe[1]);

    char ready;

    assert(read(ready_pipe[0], &ready, 1) == 1);

    /* enviar violação */
    kill(pid, SIGUSR1);

    sleep(1);

    /* processo deve continuar vivo */
    assert(kill(pid, 0) == 0);

    /* libertar step */
    kill(pid, SIGUSR2);

    FlightPosition pos;

    assert(read(pipefd[0], &pos, sizeof(pos)) == sizeof(pos));

    printf("[OK] SIGUSR1 does not interrupt flight execution\n");

    kill(pid, SIGTERM);

    waitpid(pid, NULL, 0);
}

void test_flight_sigusr2_step() {

    printf("\nTESTE2\n");

    int pipefd[2];
    int ready_pipe[2];

    assert(pipe(pipefd) == 0);
    assert(pipe(ready_pipe) == 0);

    FlightPlan plan = make_plan();

    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {

        close(pipefd[0]);
        close(ready_pipe[0]);

        run_flight(&plan, pipefd[1], ready_pipe[1], 1);

        exit(0);
    }

    close(pipefd[1]);
    close(ready_pipe[1]);

    /* esperar ready */
    char ready;
    assert(read(ready_pipe[0], &ready, 1) == 1);

    /* autorizar step */
    kill(pid, SIGUSR2);

    FlightPosition pos;

    ssize_t n = read(pipefd[0], &pos, sizeof(pos));

    assert(n == sizeof(pos));

    assert(pos.flightId == 1);

    printf("[OK] SIGUSR2 releases one simulation step\n");

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);
}

void test_multiple_steps() {

    printf("\nTESTE4\n");

    int pipefd[2];
    int ready_pipe[2];

    assert(pipe(pipefd) == 0);
    assert(pipe(ready_pipe) == 0);

    FlightPlan plan = make_plan();

    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {

        close(pipefd[0]);
        close(ready_pipe[0]);

        run_flight(&plan, pipefd[1], ready_pipe[1], 1);

        exit(0);
    }

    close(pipefd[1]);
    close(ready_pipe[1]);

    for (int i = 0; i < 3; i++) {

        char ready;

        assert(read(ready_pipe[0], &ready, 1) == 1);

        kill(pid, SIGUSR2);

        FlightPosition pos;

        assert(read(pipefd[0], &pos, sizeof(pos)) == sizeof(pos));
    }

    printf("[OK] multiple synchronized steps work\n");

    kill(pid, SIGTERM);

    waitpid(pid, NULL, 0);
}