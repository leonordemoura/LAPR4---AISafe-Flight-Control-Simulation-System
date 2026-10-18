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
#include "../include/controller.h"

static SimulationConfig base_valid_cfg() {
    SimulationConfig cfg = {
        .startTime = 0,
        .endTime = 10,
        .numFlights = 2,
        .minX = 0,
        .maxX = 90,
        .minY = 0,
        .maxY = 100,
        .weatherCondition = WEATHER_CLEAR,
        .simulationSpeed = 1,
        .updateInterval = 1
    };
    return cfg;
}

void test_valid_config() {

    printf("\nTESTE1\n");

    SimulationConfig cfg = base_valid_cfg();

    assert(validate_config(cfg) == 1);

    printf("[OK] valid config\n");
}

void test_invalid_time() {

    printf("\nTESTE2\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.startTime = 10;
    cfg.endTime = 5;

    assert(validate_config(cfg) == 0);

    printf("[OK] invalid time\n");
}

void test_invalid_numFlights() {

    printf("\nTESTE3\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.numFlights = 0;

    assert(validate_config(cfg) == 0);

    printf("[OK] invalid numFlights\n");
}

void test_invalid_area() {

    printf("\nTESTE4\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.minX = 100;
    cfg.maxX = 50;   // inválido
    cfg.minY = 0;
    cfg.maxY = 100;

    assert(validate_config(cfg) == 0);

    printf("[OK] invalid geographic area\n");
}

void test_invalid_weather() {

    printf("\nTESTE5\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.weatherCondition = 999; // fora do enum

    assert(validate_config(cfg) == 0);

    printf("[OK] invalid weather condition\n");
}

void test_invalid_performance() {

    printf("\nTESTE7\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.simulationSpeed = 0;
    assert(validate_config(cfg) == 0);

    cfg = base_valid_cfg();
    cfg.updateInterval = 0;
    assert(validate_config(cfg) == 0);

    printf("[OK] invalid performance\n");
}

// -------------------- TEST 8: SIMULATION SPEED TOO HIGH --------------------
void test_simulation_speed_too_high() {

    printf("\nTESTE8\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.simulationSpeed = 5000;

    assert(validate_config(cfg) == 0);

    printf("[OK] simulation speed too high\n");
}

// -------------------- TEST 9: UPDATE INTERVAL TOO LARGE --------------------
void test_update_interval_too_large() {

    printf("\nTESTE9\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.updateInterval = 120;

    assert(validate_config(cfg) == 0);

    printf("[OK] update interval too large\n");
}

// -------------------- TEST 10: GEOGRAPHIC BOUNDS OUT OF RANGE --------------------
void test_geographic_bounds_out_of_range() {

    printf("\nTESTE10\n");

    SimulationConfig cfg = base_valid_cfg();

    cfg.minX = -100; // inválido latitude
    cfg.maxX = 50;

    assert(validate_config(cfg) == 0);

    cfg = base_valid_cfg();

    cfg.minY = -200; // inválido longitude
    cfg.maxY = 50;

    assert(validate_config(cfg) == 0);

    printf("[OK] geographic bounds out of range\n");
}

// -------------------- TEST 11: VALID RULES --------------------
void test_valid_rules() {

    printf("\nTESTE11\n");

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 300,
        .violation_threshold = 5
    };

    assert(validate_rules(rules) == 1);

    printf("[OK] valid rules\n");
}

// -------------------- TEST 12: INVALID HORIZONTAL SEPARATION --------------------
void test_invalid_horizontal_separation() {

    printf("\nTESTE12\n");

    SafetyRules rules = {
        .horizontal_separation = -1,
        .vertical_separation = 300,
        .violation_threshold = 5
    };

    assert(validate_rules(rules) == 0);

    printf("[OK] invalid horizontal separation\n");
}

// -------------------- TEST 13: INVALID VERTICAL SEPARATION --------------------
void test_invalid_vertical_separation() {

    printf("\nTESTE13\n");

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = -50,
        .violation_threshold = 5
    };

    assert(validate_rules(rules) == 0);

    printf("[OK] invalid vertical separation\n");
}

// -------------------- TEST 14: INVALID THRESHOLD --------------------
void test_invalid_threshold() {

    printf("\nTESTE14\n");

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 300,
        .violation_threshold = 0
    };

    assert(validate_rules(rules) == 0);

    printf("[OK] invalid threshold\n");
}

// -------------------- TEST 15: VALID FLIGHT PLAN --------------------
void test_valid_plan() {

    printf("\nTESTE15\n");

    FlightPlan plan = {0};

    plan.id = 1;
    plan.numSegments = 1;

    strcpy(plan.segments[0].mode, "cruise");

    assert(validate_plan(&plan) == 1);

    printf("[OK] valid flight plan\n");
}

// -------------------- TEST 16: INVALID FLIGHT ID --------------------
void test_invalid_flight_id() {

    printf("\nTESTE16\n");

    FlightPlan plan = {0};

    plan.id = 0;
    plan.numSegments = 1;

    strcpy(plan.segments[0].mode, "cruise");

    assert(validate_plan(&plan) == 0);

    printf("[OK] invalid flight id\n");
}

// -------------------- TEST 17: NO SEGMENTS --------------------
void test_no_segments() {

    printf("\nTESTE17\n");

    FlightPlan plan = {0};

    plan.id = 1;
    plan.numSegments = 0;

    assert(validate_plan(&plan) == 0);

    printf("[OK] no segments\n");
}

// -------------------- TEST 18: INVALID SEGMENT MODE --------------------
void test_invalid_segment_mode() {

    printf("\nTESTE18\n");

    FlightPlan plan = {0};

    plan.id = 1;
    plan.numSegments = 1;

    strcpy(plan.segments[0].mode, "INVALID");

    assert(validate_plan(&plan) == 0);

    printf("[OK] invalid segment mode\n");
}

// -------------------- TEST 19: VALID AREA BOUNDS --------------------
void test_valid_area_bounds() {

    printf("\nTESTE19\n");

    SimulationConfig cfg = base_valid_cfg();

    FlightPlan plan = {0};

    plan.id = 1;
    plan.numSegments = 1;

    plan.segments[0].start.latitude = 10;
    plan.segments[0].start.longitude = 10;

    plan.segments[0].end.latitude = 20;
    plan.segments[0].end.longitude = 20;

    assert(validate_areaBounds(&plan, cfg) == 1);

    printf("[OK] valid area bounds\n");
}

// -------------------- TEST 20: INVALID AREA BOUNDS --------------------
void test_invalid_area_bounds() {

    printf("\nTESTE20\n");

    SimulationConfig cfg = base_valid_cfg();

    FlightPlan plan = {0};

    plan.id = 1;
    plan.numSegments = 1;

    // fora da área
    plan.segments[0].start.latitude = 200;
    plan.segments[0].start.longitude = 10;

    plan.segments[0].end.latitude = 20;
    plan.segments[0].end.longitude = 20;

    assert(validate_areaBounds(&plan, cfg) == 0);

    printf("[OK] invalid area bounds\n");
}

