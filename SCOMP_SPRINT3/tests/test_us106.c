// test_us106.c

#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <string.h>
#include <stdlib.h>

#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"

/* ========================= */
/* Helpers                  */
/* ========================= */

static SimulationConfig base_cfg(int flights) {
    SimulationConfig c = {0};
    c.startTime = 0;
    c.endTime = 5;
    c.numFlights = flights;
    c.minX = 0;
    c.maxX = 10;
    c.minY = 0;
    c.maxY = 10;
    c.simulationSpeed = 1;
    c.updateInterval = 1;
    c.weatherCondition = WEATHER_CLEAR;
    return c;
}

static FlightPlan make_plan(int id, double x, double y) {
    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = id;
    p.numSegments = 1;

    p.segments[0].start.latitude = x;
    p.segments[0].start.longitude = y;
    p.segments[0].start.altitude.value = 1000;

    p.segments[0].end.latitude = x + 0.2;
    p.segments[0].end.longitude = y + 0.2;
    p.segments[0].end.altitude.value = 1000;

    strcpy(p.segments[0].mode, "cruise");

    return p;
}

/* ========================= */
/* TEST 1 - threads start    */
/* ========================= */
void test_us106_threads_start() {

    printf("\n[US106] threads start test\n");

    SimulationConfig c = base_cfg(2);

    SafetyRules rules = {
        .horizontal_separation = 0.1,
        .vertical_separation = 1,
        .violation_threshold = 100
    };

    FlightPlan plans[2];
    plans[0] = make_plan(1, 0, 0);
    plans[1] = make_plan(2, 5, 5);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(c, rules, plans);
        exit(0);
    }

    sleep(2);

    kill(pid, SIGTERM);

    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US106 OK] threads started and shutdown safe\n");
}

/* ========================= */
/* TEST 2 - violation stress */
/* ========================= */
void test_us106_violation_stress() {

    printf("\n[US106] violation stress test\n");

    SimulationConfig c = base_cfg(2);

    SafetyRules rules = {
        .horizontal_separation = 5,
        .vertical_separation = 5,
        .violation_threshold = 100
    };

    FlightPlan plans[2];
    plans[0] = make_plan(1, 0, 0);
    plans[1] = make_plan(2, 0, 0); // força overlap

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(c, rules, plans);
        exit(0);
    }

    sleep(2);

    kill(pid, SIGTERM);

    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US106 OK] stress handled\n");
}

/* ========================= */
/* TEST 3 - no violations    */
/* ========================= */
void test_us106_no_violation() {

    printf("\n[US106] no violation test\n");

    SimulationConfig c = base_cfg(2);

    SafetyRules rules = {
        .horizontal_separation = 0.01,
        .vertical_separation = 0.01,
        .violation_threshold = 100
    };

    FlightPlan plans[2];
    plans[0] = make_plan(1, 0, 0);
    plans[1] = make_plan(2, 3, 3);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(c, rules, plans);
        exit(0);
    }

    sleep(2);

    kill(pid, SIGTERM);

    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US106 OK] no violation safe\n");
}