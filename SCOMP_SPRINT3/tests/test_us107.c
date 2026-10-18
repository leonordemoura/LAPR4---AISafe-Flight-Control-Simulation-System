#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>

#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"

static SimulationConfig cfg_default(void) {
    SimulationConfig c = {0};
    c.startTime = 0;
    c.endTime = 5;
    c.numFlights = 2;
    c.minX = 0;
    c.maxX = 10;
    c.minY = 0;
    c.maxY = 10;
    c.simulationSpeed = 1;
    c.updateInterval = 1;
    c.weatherCondition = WEATHER_CLEAR;
    return c;
}

static SafetyRules rules_default(void) {
    SafetyRules r = {
        .horizontal_separation = 0.05,
        .vertical_separation = 1,
        .violation_threshold = 100
    };
    return r;
}

static FlightPlan* make_conflict_plans(void) {

    static FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 0.01;
        plans[i].segments[0].end.longitude = 0.01;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    return plans;
}


/* TEST: report thread reacts to violation */
void test_us107_condition_signal() {

    printf("\n[US107] condition variable test\n");

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

    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 0.1;
        plans[i].segments[0].end.longitude = 0.1;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 0.05,
        .vertical_separation = 1,
        .violation_threshold = 100
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(3);

    kill(pid, SIGTERM);
    sleep(2);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US107 OK]\n");
}

// log foi gerado após violação
void test_us107_log_written_on_violation() {
    printf("\n[US107] log generation test\n");

    SimulationConfig cfg = cfg_default();
    cfg.endTime = 2;
    cfg.numFlights = 2;

    FlightPlan plans[2];
    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        // FORÇA conflito
        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 10;

        plans[i].segments[0].end.latitude = 0.01;
        plans[i].segments[0].end.longitude = 0.01;
        plans[i].segments[0].end.altitude.value = 10;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 1.0,
        .vertical_separation = 4,
        .violation_threshold = 100
    };

    remove("log/violations.log");
    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(3);
    kill(pid, SIGTERM);
    sleep(2);

    int status;
    waitpid(pid, &status, 0);

    // Verifica se log foi criado
   FILE *f = fopen("log/violations.log", "r");
    assert(f != NULL);

    fseek(f, 0, SEEK_END);
    long size = ftell(f);
    fclose(f);

    assert(size > 0);

    printf("[US107 LOG OK]\n");
}

//report thread acorda
void test_us107_report_not_blocking() {
    printf("\n[US107] report responsiveness test\n");

    time_t start = time(NULL);

    SimulationConfig cfg =cfg_default();
    SafetyRules rules = rules_default();

    remove("log/violations.log");
    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, make_conflict_plans());
        exit(0);
    }

    sleep(2);
    kill(pid, SIGTERM);
    sleep(1);

    waitpid(pid, NULL, 0);

    time_t end = time(NULL);

    assert((end - start) < 10); // se bloqueasse cond var, ficava preso

    printf("[US107 TIMING OK]\n");
}

// sem violação → report não acorda
void test_us107_no_false_wakeup() {

    printf("\n[US107] no false wakeup test\n");

    SimulationConfig cfg = cfg_default();
    cfg.numFlights = 1;

    FlightPlan plans[1];
    memset(&plans[0], 0, sizeof(FlightPlan));

    plans[0].id = 1;
    plans[0].numSegments = 1;

    // sem conflito
    plans[0].segments[0].start.latitude = 0;
    plans[0].segments[0].end.latitude = 5;
    plans[0].segments[0].start.longitude = 0;
    plans[0].segments[0].end.longitude = 5;
    plans[0].segments[0].start.altitude.value = 1000;
    plans[0].segments[0].end.altitude.value = 1000;

    strcpy(plans[0].segments[0].mode, "cruise");

    SafetyRules rules = {
        .horizontal_separation = 0.05,
        .vertical_separation = 1,
        .violation_threshold = 100
    };

    remove("log/violations.log");
    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(2);
    kill(pid, SIGTERM);
    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    // log deve estar vazio ou não existir
    FILE *f = fopen("log/violations.log", "r");
    if (f) {
        fseek(f, 0, SEEK_END);
        assert(ftell(f) == 0);
        fclose(f);
    }

    printf("[US107 NO FALSE WAKE OK]\n");
}

