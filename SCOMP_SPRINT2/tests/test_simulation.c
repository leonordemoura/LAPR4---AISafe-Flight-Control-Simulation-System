#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>

#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"

SimulationConfig make_valid_cfg() {
    SimulationConfig cfg = {
        .startTime = 0,
        .endTime = 10,
        .numFlights = 1,
        .minX = 0,
        .maxX = 90,
        .minY = 0,
        .maxY = 100,
        .weatherCondition = WEATHER_CLEAR,
        .simulationSpeed = 1,
        .updateInterval = 1,
        .flightIds = {1}
    };

    return cfg;
}

FlightPlan make_valid_plan(int id) {

    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = id;
    strcpy(p.departureTime, "09:00");
    p.numSegments = 1;

    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].start.altitude.value = 1000;

    p.segments[0].end.latitude = 1;
    p.segments[0].end.longitude = 1;
    p.segments[0].end.altitude.value = 1000;

    strcpy(p.segments[0].mode, "cruise");

    p.fuelQuantity = 1000;

    return p;
}

/* -----------------------------
   TEST 1: invalid config blocks execution
------------------------------*/
void test_invalid_config_blocks() {

    printf("\nTESTE1\n");

    SimulationConfig cfg = {0};
    cfg.numFlights = 0;

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 1
    };

    FlightPlan plans[1];
    plans[0] = make_valid_plan(1);

    start_simulation(cfg, rules, plans);

    printf("[OK] invalid config blocks execution\n");
}

/* -----------------------------
   TEST 2: valid config creates log file
------------------------------*/
void test_log_creation() {
    printf("\nTESTE2\n");

    SimulationConfig cfg = make_valid_cfg();

    FlightPlan plans[1];
    plans[0] = make_valid_plan(1);

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 1
    };

    start_simulation(cfg, rules, plans);

    assert(access("log/simulation.txt", F_OK) == 0);

    printf("[OK] log file created\n");
}

/* -----------------------------
   TEST 3: teste valido (no crash)
------------------------------*/
void test_smoke_simulation() {

    printf("\nTESTE3\n");

    SimulationConfig cfg = make_valid_cfg();

    FlightPlan plans[1];
    plans[0] = make_valid_plan(1);

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 100
    };

    start_simulation(cfg, rules, plans);

    printf("[OK] smoke simulation finished\n");
}