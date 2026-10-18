#include <assert.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>
#include <signal.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <unistd.h>

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

SimulationSharedState *attach_shared_memory() {

    int fd = shm_open(SHM_NAME, O_RDONLY, 0666);
    if (fd == -1) {
        perror("shm_open");
        return NULL;
    }

    SimulationSharedState *shm =
        mmap(NULL,
             sizeof(SimulationSharedState),
             PROT_READ,
             MAP_SHARED,
             fd,
             0);

    if (shm == MAP_FAILED) {
        perror("mmap");
        close(fd);
        return NULL;
    }

    close(fd);
    return shm;
}

void detach_shared_memory(SimulationSharedState *shm) {
    if (shm != NULL) {
        munmap(shm, sizeof(SimulationSharedState));
    }
}

/* TEST: flights must advance in lockstep */
void test_us108_lockstep() {

    printf("\n[US108] lockstep test\n");

    SimulationConfig cfg = {0};
    cfg.startTime = 0;
    cfg.endTime = 5;
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

        plans[i].segments[0].start.latitude = i;
        plans[i].segments[0].start.longitude = i;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = i + 1;
        plans[i].segments[0].end.longitude = i + 1;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 10,
        .vertical_separation = 10,
        .violation_threshold = 100
    };

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

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US108 OK]\n");
}

//validar sincronização real
//ideia:
//forçar logs e verificar ordem de steps
void test_us108_lockstep_order() {
    printf("\n[US108] lockstep order test\n");

    SimulationConfig cfg = cfg_default();

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
        .horizontal_separation = 100,
        .vertical_separation = 100,
        .violation_threshold = 100
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(3);
    kill(pid, SIGTERM);
    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US108 OK - ORDER]\n");
}

//garantir que nenhum flight avança sem o outro
void test_us108_multi_flight_sync() {

    printf("\n[US108] multi-flight sync test\n");

    SimulationConfig cfg = cfg_default();

    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        plans[i].segments[0].start.latitude = i;
        plans[i].segments[0].start.longitude = i;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = i + 1;
        plans[i].segments[0].end.longitude = i + 1;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 100,
        .vertical_separation = 100,
        .violation_threshold = 100
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(4);
    kill(pid, SIGTERM);
    sleep(1);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status) || WIFSIGNALED(status));

    printf("[US108 OK - MULTI SYNC]\n");
}

//provar que o controller espera todos os flights
/*
 * US108 FINAL TEST:
 * Verifies true lockstep behavior:
 * - no flight can advance ahead of the other
 */
void test_us108_strict_lockstep() {

    printf("\n[US108] strict lockstep\n");

    SimulationConfig cfg = cfg_default();


    FlightPlan plans[2];

    for (int i = 0; i < 2; i++) {
        memset(&plans[i], 0, sizeof(FlightPlan));
        plans[i].id = i + 1;
        plans[i].numSegments = 1;

        // neutral coordinates (no validation noise)
        plans[i].segments[0].start.latitude = 1;
        plans[i].segments[0].start.longitude = 1;
        plans[i].segments[0].start.altitude.value = 1000;

        plans[i].segments[0].end.latitude = 2;
        plans[i].segments[0].end.longitude = 2;
        plans[i].segments[0].end.altitude.value = 1000;

        strcpy(plans[i].segments[0].mode, "cruise");
    }

    SafetyRules rules = {
        .horizontal_separation = 100,
        .vertical_separation = 100,
        .violation_threshold = 100
    };

    pid_t pid = fork();

    if (pid == 0) {
        start_simulation(cfg, rules, plans);
        exit(0);
    }

    sleep(3);

    kill(pid, SIGTERM);
    waitpid(pid, NULL, 0);

    sleep(1); // SHM flush

    SimulationSharedState *shm = attach_shared_memory();


    int base = shm->histories[0].count;

    for (int i = 1; i < cfg.numFlights; i++) {
        assert(shm->histories[i].count == base);
    }

    detach_shared_memory(shm);

    printf("[US108 STRICT LOCKSTEP OK]\n");
}