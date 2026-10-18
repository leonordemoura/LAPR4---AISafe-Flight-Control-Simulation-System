#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>
#include <time.h>
#include <errno.h>
#include <math.h>

#include "../include/common.h"
#include "../include/flight.h"
#include "../include/controller.h"
#include "../include/util.h"

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

// ---------------- helper: fake segment ----------------
static int advance_one_step(pid_t pid,
                            int ready_fd,
                            int pos_fd,
                            FlightPosition *pos)
{
    char ready;

    if (read(ready_fd, &ready, 1) != 1)
        return 0;

    kill(pid, SIGUSR2);

    if (read(pos_fd, pos, sizeof(*pos)) != sizeof(*pos))
        return 0;

    return 1;
}

// ---------------- TEST 1: NULL PLAN ----------------
void test_flight_null_plan() {
    int pipe_pos[2];
    int pipe_ready[2];

    pipe(pipe_pos);
    pipe(pipe_ready);

    pid_t pid = fork();

    if (pid == 0) {
        close(pipe_pos[0]);
        close(pipe_ready[0]);

        run_flight(NULL, pipe_pos[1], pipe_ready[1], 1);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));
    assert(WEXITSTATUS(status) == 1);

    printf("TEST NULL PLAN OK\n");
}

// ---------------- TEST 2: PIPE OUTPUT ----------------
void test_flight_pipe_output() {
    int pipe_pos[2];
    int pipe_ready[2];

    pipe(pipe_pos);
    pipe(pipe_ready);

    FlightPlan plan = make_plan();

    pid_t pid = fork();

    if (pid == 0) {
        close(pipe_pos[0]);
        close(pipe_ready[0]);

        run_flight(&plan, pipe_pos[1], pipe_ready[1], 1);
        exit(0);
    }

    close(pipe_pos[1]);
    close(pipe_ready[1]);

    FlightPosition pos;

    assert(advance_one_step(pid, pipe_ready[0], pipe_pos[0], &pos));

    assert(pos.flightId == 1);
    assert(pos.x >= 0.0 && pos.x <= 1.0);
    assert(pos.y >= 0.0 && pos.y <= 1.0);
    assert(pos.altitude >= 1000.0 && pos.altitude <= 2000.0);

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);

    printf("TEST PIPE OUTPUT OK\n");
}

// ---------------- TEST 3: MONOTONIC TIMESTAMP ----------------
void test_flight_timestamp_order() {
    int pipe_pos[2];
    int pipe_ready[2];

    pipe(pipe_pos);
    pipe(pipe_ready);

    FlightPlan plan = make_plan();

    pid_t pid = fork();

    if (pid == 0) {
        close(pipe_pos[0]);
        close(pipe_ready[0]);

        run_flight(&plan, pipe_pos[1], pipe_ready[1], 1);
        exit(0);
    }

    close(pipe_pos[1]);
    close(pipe_ready[1]);

    FlightPosition prev, cur;

    assert(advance_one_step(pid, pipe_ready[0], pipe_pos[0], &prev));

    for (int i = 0; i < 5; i++) {
        assert(advance_one_step(pid, pipe_ready[0], pipe_pos[0], &cur));
        assert(cur.timestamp >= prev.timestamp);
        prev = cur;
    }

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);

    printf("TEST TIMESTAMP ORDER OK\n");
}

// ---------------- TEST 4: SIGTERM STOPS EARLY ----------------
void test_flight_sigterm_stop() {
    int pipe_pos[2];
    int pipe_ready[2];

    pipe(pipe_pos);
    pipe(pipe_ready);

    FlightPlan plan = make_plan();

    pid_t pid = fork();

    if (pid == 0) {
        close(pipe_pos[0]);
        close(pipe_ready[0]);

        run_flight(&plan, pipe_pos[1], pipe_ready[1], 1);
        exit(0);
    }

    close(pipe_pos[1]);
    close(pipe_ready[1]);

    char ready;
    ssize_t n = read(pipe_ready[0], &ready, 1);

    assert(n == 1); // flight is ready for the first step

    kill(pid, SIGTERM);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("TEST SIGTERM STOP OK\n");
}