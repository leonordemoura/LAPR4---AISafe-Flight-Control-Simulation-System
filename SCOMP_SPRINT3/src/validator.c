#include <stdio.h>
#include <string.h>
#include "common.h"
#include "controller.h"

int validate_config(SimulationConfig config) {

    // validar tempo de simulação
    if (config.simulationSpeed > 1000) {
        printf("Simulation speed too high\n");
        return 0;
    }

    // validar intervalo de atualização
    if (config.updateInterval > 60) {
        printf("Update interval too large\n");
        return 0;
    }

    // validar tempo
    if (config.startTime < 0 ||
        config.endTime <= config.startTime) {

        printf("Invalid time range\n");
        return 0;
    }

    // validar número de voos
    if (config.numFlights <= 0 ||
        config.numFlights > MAX_FLIGHTS) {

        printf("Invalid number of flights\n");
        return 0;
    }

    // validar área geográfica
    if (config.minX >= config.maxX ||
        config.minY >= config.maxY) {

        printf("Invalid geographic area\n");
        return 0;
    }

    if (config.minX < -90 || config.maxX > 90 ||
        config.minY < -180 || config.maxY > 180) {
        printf("Geographic bounds out of range\n");
        return 0;
    }

    // validar weather condition
    if (config.weatherCondition < WEATHER_CLEAR ||
        config.weatherCondition > WEATHER_STORM) {

        printf("Invalid weather condition\n");
        return 0;
    }

    // validar performance
    if (config.simulationSpeed <= 0) {

        printf("Invalid simulation speed\n");
        return 0;
    }

    if (config.updateInterval <= 0) {

        printf("Invalid update interval\n");
        return 0;
    }

    return 1;
}

int validate_areaBounds(const FlightPlan *plans, SimulationConfig config) {

    for (int s = 0; s < plans->numSegments; s++) {
        const FlightSegment *seg = &plans->segments[s];

        if (seg->start.latitude < config.minX ||
            seg->start.latitude > config.maxX ||
            seg->start.longitude < config.minY ||
            seg->start.longitude > config.maxY) {

            printf("Flight %d segment %d start out of bounds\n", plans->id, s);
            return 0;
        }

        if (seg->end.latitude < config.minX ||
            seg->end.latitude > config.maxX ||
            seg->end.longitude < config.minY ||
            seg->end.longitude > config.maxY) {

            printf("Flight %d segment %d end out of bounds\n", plans->id, s);
            return 0;
        }
    }

    return 1;
}

int validate_plan(const FlightPlan *plan){

    if(plan->id <= 0){
        printf("Invalid flight ID\n");
        return 0;
    }

    if (plan->numSegments <= 0) {
        printf("Flight %d has no segments\n", plan->id);
        return 0;
    }

    for (int s = 0; s < plan->numSegments; s++) {
        const FlightSegment *seg = &plan->segments[s];

        if (strcmp(seg->mode, "climb") != 0 &&
            strcmp(seg->mode, "cruise") != 0 &&
            strcmp(seg->mode, "descend") != 0) {

            printf("Flight %d segment %d has invalid mode\n", plan->id, s);
            return 0;
        }
    }

    return 1;

}

int validate_rules(SafetyRules rules) {

    if(rules.horizontal_separation < 0){
        printf("Invalid horizontal separation\n");
        return 0;
    }

    if(rules.vertical_separation < 0){
        printf("Invalid vertical separation\n");
        return 0;
    }

    if(rules.violation_threshold <= 0){
        printf("Invalid threshold\n");
        return 0;
    }

    return 1;
}