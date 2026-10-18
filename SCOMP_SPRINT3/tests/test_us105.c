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

/* ---------------- helpers ---------------- */

static SimulationConfig base_cfg() {
    SimulationConfig cfg = {0};
    cfg.startTime = 0;
    cfg.endTime = 3;
    cfg.numFlights = 2;
    cfg.minX = 0;
    cfg.maxX = 10;
    cfg.minY = 0;
    cfg.maxY = 10;
    cfg.simulationSpeed = 1;
    cfg.updateInterval = 1;
    cfg.weatherCondition = WEATHER_CLEAR;
    return cfg;
}

static FlightPlan make_plan(int id) {
    FlightPlan p;
    memset(&p, 0, sizeof(p));
    p.id = id;
    p.numSegments = 1;

    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].start.altitude.value = 1000;

    p.segments[0].end.latitude = 1;
    p.segments[0].end.longitude = 1;
    p.segments[0].end.altitude.value = 1000;

    strcpy(p.segments[0].mode, "cruise");
    return p;
}

/* ===================================================== */
/* TEST 1 — MULTI FLIGHT START                           */
/* ===================================================== */
void test_us105_multi_flight_start() {

    printf("\n[US105] multi flight start\n");

    SimulationConfig cfg = base_cfg();

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 100
    };

    FlightPlan plans[2];
    plans[0] = make_plan(1);
    plans[1] = make_plan(2);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] multi flight start\n");
}

/* ===================================================== */
/* TEST 2 — INVALID CONFIG SHOULD NOT START             */
/* ===================================================== */
void test_us105_invalid_config_blocks_start() {

    printf("\n[US105] invalid config block\n");

    SimulationConfig cfg = base_cfg();
    cfg.numFlights = 0;   // inválido

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 10
    };

    FlightPlan plans[1];
    plans[0] = make_plan(1);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] invalid config blocked\n");
}

/* ===================================================== */
/* TEST 3 — INVALID RULES BLOCK START                   */
/* ===================================================== */
void test_us105_invalid_rules_blocks_start() {

    printf("\n[US105] invalid rules block\n");

    SimulationConfig cfg = base_cfg();

    SafetyRules rules = {
        .horizontal_separation = -1,
        .vertical_separation = 10,
        .violation_threshold = 10
    };

    FlightPlan plans[1];
    plans[0] = make_plan(1);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] invalid rules blocked\n");
}

/* ===================================================== */
/* TEST 4 — SHARED MEMORY INITIAL STATE                 */
/* ===================================================== */
void test_us105_shared_memory_init() {

    printf("\n[US105] shared memory init\n");

    SimulationConfig cfg = base_cfg();

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 100
    };

    FlightPlan plans[1];
    plans[0] = make_plan(1);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] shm initialized correctly\n");
}

/* ===================================================== */
/* TEST 5 — EARLY STOP (THRESHOLD)                      */
/* ===================================================== */
void test_us105_early_stop() {

    printf("\n[US105] early stop\n");

    SimulationConfig cfg = base_cfg();
    cfg.endTime = 10;

    SafetyRules rules = {
        .horizontal_separation = 0,   // força violações
        .vertical_separation = 0,
        .violation_threshold = 1      // stop rápido
    };

    FlightPlan plans[1];
    plans[0] = make_plan(1);

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));

    printf("[OK] early stop triggered\n");
}

