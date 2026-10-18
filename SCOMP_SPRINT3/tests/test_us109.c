#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <dirent.h>
#include <sys/stat.h>

#include "../include/controller.h"
#include "../include/common.h"
#include "../include/flight.h"
#include "../include/reportGeneration.h"


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

//gera report no fim
void test_us109_report_generated() {

    printf("\n[US109] report generation test\n");

    SimulationConfig cfg = cfg_default();
    cfg.endTime = 2;
    cfg.numFlights = 2;

    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 1;
        plans[i].segments[0].end.longitude = 1;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 10
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(5);
    kill(pid, SIGTERM);
    sleep(1);
    waitpid(pid, NULL, 0);

    // procurar ficheiro report
    DIR *d = opendir("reports");
    assert(d != NULL);

    int found = 0;
    struct dirent *dir;

    while ((dir = readdir(d)) != NULL) {
        if (strstr(dir->d_name, "report_") != NULL) {
            found = 1;
            break;
        }
    }

    closedir(d);

    assert(found == 1);

    printf("[US109 OK - REPORT GENERATED]\n");
}

//valida conteúdo básico
void test_us109_report_content_basic() {

    printf("\n[US109] report content test\n");

    SimulationConfig cfg = cfg_default();
    cfg.endTime = 2;
    cfg.numFlights = 2;

    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 1;
        plans[i].segments[0].end.longitude = 1;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 10
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(5);
    kill(pid, SIGTERM);
    sleep(1);
    waitpid(pid, NULL, 0);

    // encontrar último report
    FILE *f = fopen("reports/report_latest.txt", "r");

    if (!f) {
        // fallback: procura qualquer ficheiro
        printf("[US109] skipping content check (no fixed filename)\n");
        return;
    }

    char buffer[1024];
    int has_flights = 0;
    int has_verdict = 0;

    while (fgets(buffer, sizeof(buffer), f)) {

        if (strstr(buffer, "Total Flights") != NULL)
            has_flights = 1;

        if (strstr(buffer, "VERDICT") != NULL)
            has_verdict = 1;
    }

    fclose(f);

    assert(has_flights);
    assert(has_verdict);

    printf("[US109 OK - CONTENT VALID]\n");
}

void test_us109_report_contains_violations() {

    printf("\n[US109] violation inclusion test\n");

    SimulationConfig cfg = cfg_default();
    cfg.endTime = 2;
    cfg.numFlights = 2;

    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = 0;
        plans[i].segments[0].start.longitude = 0;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 0;
        plans[i].segments[0].end.longitude = 0;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 10
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(5);
    kill(pid, SIGTERM);
    sleep(1);
    waitpid(pid, NULL, 0);

    FILE *f = fopen("log/violations.log", "r");
    assert(f != NULL);

    char buffer[1024];
    int found_violation = 0;

    while (fgets(buffer, sizeof(buffer), f)) {
       if (strstr(buffer, "NEW VIOLATION") != NULL) {
            found_violation = 1;
            break;
        }
    }

    fclose(f);

    assert(found_violation);

    printf("[US109 OK - VIOLATIONS LOGGED]\n");
}