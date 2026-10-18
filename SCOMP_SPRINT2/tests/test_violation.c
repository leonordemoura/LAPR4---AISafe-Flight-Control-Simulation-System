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

#define FUEL_CLIMB_KG_PER_KM   6.0
#define FUEL_CRUISE_KG_PER_KM  3.2
#define FUEL_DESCEND_KG_PER_KM 2.0

// helper: cria posição
static FlightPosition make_pos(int id, double x, double y, double alt, long ts) {
    FlightPosition p;
    p.flightId = id;
    p.x = x;
    p.y = y;
    p.altitude = alt;
    p.timestamp = ts;
    return p;
}

// helper: cria history com 1 posição
static void add_pos(FlightHistory *h, FlightPosition p) {
    h->positions[h->count++] = p;
}

// -------------------- TEST 1: POSITIVO --------------------
void test_violation_positive() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 5, 5, 1100, 1000)); // CLOSE → violation

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 1);
    assert(res == 0);

    printf("TEST POSITIVE OK\n");
}

// -------------------- TEST 2: HORIZONTAL SAFE --------------------
void test_violation_horizontal_safe() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 100, 100, 1100, 1000)); // FAR → no violation

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 0);
    assert(res == 0);

    printf("TEST HORIZONTAL SAFE OK\n");
}

// -------------------- TEST 3: ALTITUDE SAFE --------------------
void test_violation_altitude_safe() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 5, 5, 2000, 1000)); // altitude too far

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 0);
    assert(res == 0);

    printf("TEST ALTITUDE SAFE OK\n");
}

// -------------------- TEST 4: TIME SAFE --------------------
void test_violation_time_safe() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 5, 5, 1100, 5000)); // time diff > 60

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 0);
    assert(res == 0);

    printf("TEST TIME SAFE OK\n");
}

// -------------------- TEST 5: EDGE CASE --------------------
void test_violation_edge() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 10, 0, 1300, 1000)); // EXACT LIMIT

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 0); // because condition is < not <=
    assert(res == 0);

    printf("TEST EDGE OK\n");
}

// -------------------- TEST 6: TIME OVERLAY FAIL --------------------
void test_violation_time_overlay_fail() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation = 300.0,
        .violation_threshold = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    // Flight A (tempo t = 1000)
    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));

    // Flight B (mesma posição espacial, MAS muito longe no tempo)
    add_pos(&history[1], make_pos(2, 5, 5, 1100, 2000));
    // diff = 1000 → > 60 → NÃO pode haver violação

    SafetyViolationEvent events[10];

    FILE *log = fopen("/dev/null", "w");

    int flight_active[2] = {1, 1};
    int count = 0;

    int res = capture_violation(log,cfg,rules,history,pids,flight_active,&count,events);

    fclose(log);

    assert(count == 0);
    assert(res == 0);

    printf("TEST TIME OVERLAY FAIL OK\n");
}

// -------------------- TEST 7: TIME OVERLAY SUCCESS --------------------
void test_violation_time_overlay_success() {

    SimulationConfig cfg = {0};
    cfg.numFlights = 2;

    SafetyRules rules = {
        .horizontal_separation = 10.0,
        .vertical_separation   = 300.0,
        .violation_threshold   = 10
    };

    FlightHistory history[2] = {0};
    pid_t pids[2] = {1,2};

    add_pos(&history[0], make_pos(1, 0, 0, 1000, 1000));
    add_pos(&history[1], make_pos(2, 5, 5, 1100, 1000));

    SafetyViolationEvent events[10];
    int flight_active[2] = {1, 1};
    int count = 0;

    FILE *log = fopen("/dev/null", "w");

    int res = capture_violation(log, cfg, rules, history, pids, flight_active, &count, events);

    fclose(log);

    assert(count == 1);
    assert(res == 0);

    printf("TEST TIME OVERLAY SUCCESS OK\n");
}

void test_fuel_sufficient() {
    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = 1;
    p.fuelQuantity = 10000;

    strcpy(p.departureTime, "00:00");
    p.numSegments = 1;

    strcpy(p.segments[0].mode, "cruise");
    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].end.latitude = 1;
    p.segments[0].end.longitude = 1;

    int res = check_fuel(&p);

    assert(res == 1);
    printf("[OK] fuel sufficient\n");
}

void test_fuel_insufficient() {
    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = 2;
    p.fuelQuantity = 1; // quase nada

    strcpy(p.departureTime, "00:00");
    p.numSegments = 1;

    strcpy(p.segments[0].mode, "cruise");
    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].end.latitude = 10;
    p.segments[0].end.longitude = 10;

    int res = check_fuel(&p);

    assert(res == 0);
    printf("[OK] fuel insufficient\n");
}

void test_fuel_exact_limit() {
    FlightPlan p;
    memset(&p, 0, sizeof(p));

    p.id = 3;

    strcpy(p.departureTime, "00:00");
    p.numSegments = 1;

    strcpy(p.segments[0].mode, "cruise");

    p.segments[0].start.latitude = 0;
    p.segments[0].start.longitude = 0;
    p.segments[0].end.latitude = 1;
    p.segments[0].end.longitude = 1;

    double dist = calculate_kilometers(0,0,1,1);
    p.fuelQuantity = dist * FUEL_CRUISE_KG_PER_KM;

    int res = check_fuel(&p);

    assert(res == 1);
    printf("[OK] fuel exact limit\n");
}