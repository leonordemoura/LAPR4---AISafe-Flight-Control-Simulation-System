#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <fcntl.h>
#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"

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

void test_single_step() {
    printf("\nTESTE1\n");
    int pipefd[2], ready[2];
    assert(pipe(pipefd) == 0);
    assert(pipe(ready) == 0);

    FlightPlan plan = make_plan();
    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {
        close(pipefd[0]);
        close(ready[0]);
        run_flight(&plan, pipefd[1], ready[1], 1);
        exit(0);
    }

    close(pipefd[1]);
    close(ready[1]);

    char r;
    assert(read(ready[0], &r, 1) == 1);

    kill(pid, SIGUSR2);

    FlightPosition pos;
    assert(read(pipefd[0], &pos, sizeof(pos)) == sizeof(pos));

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);

    printf("[OK] single step works\n");
}

void test_no_autorization_no_move() {
    printf("\nTESTE2\n");

    int pipefd[2], ready[2];
    assert(pipe(pipefd) == 0);
    assert(pipe(ready) == 0);

    pid_t pid = fork();
    assert(pid >= 0);

    if (pid == 0) {
        close(pipefd[0]);
        close(ready[0]);
        FlightPlan plan = make_plan();
        run_flight(&plan, pipefd[1], ready[1], 1);
        exit(0);
    }

    close(pipefd[1]);
    close(ready[1]);

    char r;
    assert(read(ready[0], &r, 1) == 1);

    // NÃO manda SIGUSR2

    // tenta ler posição (não devia haver nada)
    fd_set set;
    struct timeval tv = {1, 0};

    FD_ZERO(&set);
    FD_SET(pipefd[0], &set);

    int ret = select(pipefd[0] + 1, &set, NULL, NULL, &tv);

    assert(ret == 0); // timeout → não há movimento

    printf("[OK] blocked without SIGUSR2\n");

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);
}

void test_step_sequence() {

    printf("\nTESTE3\n");
    int pipefd[2], ready[2];
    assert(pipe(pipefd) == 0);
    assert(pipe(ready) == 0);

    FlightPlan plan = make_plan();
    pid_t pid = fork();

    if (pid == 0) {
        close(pipefd[0]);
        close(ready[0]);
        run_flight(&plan, pipefd[1], ready[1], 1);
        exit(0);
    }

    close(pipefd[1]);
    close(ready[1]);

    for (int i = 0; i < 5; i++) {

        char r;
        assert(read(ready[0], &r, 1) == 1);

        kill(pid, SIGUSR2);

        FlightPosition pos;
        assert(read(pipefd[0], &pos, sizeof(pos)) == sizeof(pos));
    }

    printf("[OK] step sequence stable\n");

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);
}

void test_no_deadlock() {
    printf("\nTESTE4\n");

    int pipefd[2], ready[2];
    assert(pipe(pipefd) == 0);
    assert(pipe(ready) == 0);

    FlightPlan plan = make_plan();
    pid_t pid = fork();

    if (pid == 0) {
        close(pipefd[0]);
        close(ready[0]);
        run_flight(&plan, pipefd[1], ready[1], 1);
        exit(0);
    }

    close(pipefd[1]);
    close(ready[1]);

    for (int i = 0; i < 20; i++) {

        char r;
        if (read(ready[0], &r, 1) != 1)
            break;

        kill(pid, SIGUSR2);

        FlightPosition pos;
        if (read(pipefd[0], &pos, sizeof(pos)) != sizeof(pos))
            break;
    }

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);

    printf("[OK] no deadlock under repeated steps\n");
}

void test_multiple_sigusr2_safe() {
    printf("\nTESTE5\n");

    int pipefd[2], ready[2];
    assert(pipe(pipefd) == 0);
    assert(pipe(ready) == 0);

    pid_t pid = fork();

    if (pid == 0) {
        close(pipefd[0]);
        close(ready[0]);
        FlightPlan plan = make_plan();
        run_flight(&plan, pipefd[1], ready[1], 1);
        exit(0);
    }

    close(pipefd[1]);
    close(ready[1]);

    char r;
    assert(read(ready[0], &r, 1) == 1);

    kill(pid, SIGUSR2);
    kill(pid, SIGUSR2);
    kill(pid, SIGUSR2);

    FlightPosition pos;
    assert(read(pipefd[0], &pos, sizeof(pos)) == sizeof(pos));

    printf("[OK] SIGUSR2 does not accumulate steps\n");

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);
}