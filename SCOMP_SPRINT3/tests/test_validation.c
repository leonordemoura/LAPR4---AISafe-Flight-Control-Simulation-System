#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"

/* =========================
   HELPERS
========================= */

static SimulationConfig base_cfg() {
    SimulationConfig c = {
        .startTime = 0,
        .endTime = 10,
        .numFlights = 1,
        .minX = 0,
        .maxX = 90,
        .minY = 0,
        .maxY = 90,
        .weatherCondition = WEATHER_CLEAR,
        .simulationSpeed = 1,
        .updateInterval = 1
    };
    return c;
}

static FlightPlan valid_plan() {
    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = 1;
    p.numSegments = 1;

    p.segments[0].start.latitude = 10;
    p.segments[0].start.longitude = 10;
    p.segments[0].start.altitude.value = 1000;

    p.segments[0].end.latitude = 20;
    p.segments[0].end.longitude = 20;
    p.segments[0].end.altitude.value = 1000;

    strcpy(p.segments[0].mode, "cruise");

    return p;
}
/* =========================
   CONFIG TESTS
========================= */

void test_config_valid_basic() {
    SimulationConfig c = base_cfg();
    assert(validate_config(c));
    printf("[CONFIG OK] basic valid\n");
}

void test_config_invalid_speed_low() {
    SimulationConfig c = base_cfg();
    c.simulationSpeed = 0;
    assert(!validate_config(c));
    printf("[CONFIG OK] speed low rejected\n");
}

void test_config_invalid_speed_high() {
    SimulationConfig c = base_cfg();
    c.simulationSpeed = 2000;
    assert(!validate_config(c));
    printf("[CONFIG OK] speed high rejected\n");
}

void test_config_invalid_time() {
    SimulationConfig c = base_cfg();
    c.startTime = 10;
    c.endTime = 5;
    assert(!validate_config(c));
    printf("[CONFIG OK] time invalid rejected\n");
}

void test_config_invalid_flights() {
    SimulationConfig c = base_cfg();
    c.numFlights = 0;
    assert(!validate_config(c));
    printf("[CONFIG OK] flights invalid rejected\n");
}

void test_config_invalid_area() {
    SimulationConfig c = base_cfg();
    c.minX = 50;
    c.maxX = 10;
    assert(!validate_config(c));
    printf("[CONFIG OK] area invalid rejected\n");
}

/* =========================
   RULES TESTS
========================= */

void test_rules_valid() {
    SafetyRules r = {10, 10, 1};
    assert(validate_rules(r));
    printf("[RULES OK] valid\n");
}

void test_rules_invalid() {
    SafetyRules r = {-1, 10, 1};
    assert(!validate_rules(r));
    printf("[RULES OK] invalid rejected\n");
}

/* =========================
   PLAN TESTS
========================= */

void test_plan_valid() {
    FlightPlan p = valid_plan();
    assert(validate_plan(&p));
    printf("[PLAN OK] valid\n");
}

void test_plan_invalid_id() {
    FlightPlan p = valid_plan();
    p.id = 0;
    assert(!validate_plan(&p));
    printf("[PLAN OK] invalid id\n");
}

void test_plan_invalid_segments() {
    FlightPlan p = valid_plan();
    p.numSegments = 0;
    assert(!validate_plan(&p));
    printf("[PLAN OK] no segments\n");
}

void test_plan_invalid_mode() {
    FlightPlan p = valid_plan();
    strcpy(p.segments[0].mode, "invalid");
    assert(!validate_plan(&p));
    printf("[PLAN OK] invalid mode\n");
}

/* =========================
   AREA TESTS
========================= */

void test_area_valid() {
    SimulationConfig c = base_cfg();
    FlightPlan p = valid_plan();
    assert(validate_areaBounds(&p, c));
    printf("[AREA OK] valid\n");
}

void test_area_invalid_start() {
    SimulationConfig c = base_cfg();
    FlightPlan p = valid_plan();
    p.segments[0].start.latitude = -999;
    assert(!validate_areaBounds(&p, c));
    printf("[AREA OK] invalid start\n");
}

void test_area_invalid_end() {
    SimulationConfig c = base_cfg();
    FlightPlan p = valid_plan();
    p.segments[0].end.latitude = -999;
    assert(!validate_areaBounds(&p, c));
    printf("[AREA OK] invalid end\n");
}