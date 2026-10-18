#include <stdio.h>
#include <math.h>
#include <time.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/types.h>
#include <string.h>

#include "common.h"
#include "util.h"

#define FUEL_CLIMB_KG_PER_KM   6.0
#define FUEL_CRUISE_KG_PER_KM  3.2
#define FUEL_DESCEND_KG_PER_KM 2.0

/* Verifica se existiu alguma violação das regras de segurança*/
int capture_violation(FILE *log, SimulationConfig config, SafetyRules rules, FlightHistory *history, const pid_t pids[], const int flight_active[], int *violation_count, SafetyViolationEvent *events) {

    int threshold_reached = 0;

    for (int a = 0; a < config.numFlights && !threshold_reached; a++) {
        if (history[a].count == 0) continue;

        for (int b = a + 1; b < config.numFlights && !threshold_reached; b++) {
            if (history[b].count == 0) continue;

            /* Posição do Avião*/
            FlightPosition *pa = &history[a].positions[history[a].count - 1];
            int flightBIndex = find_closest_position(&history[b], pa->timestamp);
            FlightPosition *pb = (flightBIndex != -1) ? &history[b].positions[flightBIndex] : NULL;

            if (!pb) continue;

            /* Calcula a distância horizontal baseada nas coordenadas*/
            const double hdist = hypot(pa->x - pb->x, pa->y - pb->y);
            /* Calcula a distância vertical baseada na altitude*/
            const double vdist = fabs(pa->altitude - pb->altitude);

            if (hdist >= rules.horizontal_separation) continue;
            if (vdist >= rules.vertical_separation) continue;
            if(pa->timestamp != pb->timestamp) continue;

            (*violation_count)++;
            history[a].violation_count++;
            history[b].violation_count++;

            /* Verifica se o número de infrações ultrapassou o limite definido nas regras de segurança*/
            if (rules.violation_threshold > 0 && *violation_count >= rules.violation_threshold) {
                threshold_reached = 1;
            }

            /* Converts timestamp into regular Time*/
            char time_str[6], time_flightB[6];
            turn_time_into_char(pa->timestamp / 60, time_str, sizeof(time_str));
            turn_time_into_char(pb->timestamp / 60, time_flightB, sizeof(time_flightB));

            /* Calcula o vetor de velocidade*/
            VelocityVector vv_a, vv_b;
            if (history[a].count >= 2){
                compute_velocity_vector(&history[a].positions[history[a].count - 2], pa, &vv_a);
            }
            else{
                vv_a = (VelocityVector){0, 0, 0};
            }
            if (flightBIndex > 0){
                compute_velocity_vector(&history[b].positions[flightBIndex - 1], pb, &vv_b);
            }
            else{
                vv_b = (VelocityVector){0, 0, 0};
            }

            /* Guarda todas as informações sobre a Infração*/
            events[(*violation_count) - 1].flightA = pa->flightId;
            events[(*violation_count) - 1].posA = *pa;
            events[(*violation_count) - 1].velA = vv_a;
            events[(*violation_count) - 1].flightB = pb->flightId;
            events[(*violation_count) - 1].posB = *pb;
            events[(*violation_count) - 1].velB = vv_b;
            events[(*violation_count) - 1].timestamp_sec = pa->timestamp;
            events[(*violation_count) - 1].horizontal_distance = hdist;
            events[(*violation_count) - 1].vertical_distance = vdist;

            /* Envia os dados da infração para um ficheiro de Log */
            fprintf(log, "\n[VIOLATION] Time=%s\n Horizontal Distance=%.4f Vertical Distance=%.4f count=%d\n", time_str, hdist, vdist, *violation_count);
            fprintf(log, "   Flight %d : (%.2f, %.2f) on %.2f at %s\n   Flight %d : (%.2f, %.2f) on %.2f at %s", pa->flightId, pa->x, pa->y, pa->altitude, time_str, pb->flightId, pb->x, pb->y, pb->altitude, time_flightB);
            fflush(log);

            printf("[VIOLATION] flights %d & %d Horizontal Distance=%.4f Vertical Distance=%.4f (#%d)\n", pa->flightId, pb->flightId, hdist, vdist, *violation_count);

            if (flight_active[a] && kill(pids[a], SIGUSR1) == -1)
                perror("[SIM] Send SIGUSR1");
            if (flight_active[b] && kill(pids[b], SIGUSR1) == -1)
                perror("[SIM] Send SIGUSR1");


        }
    }

    /* Se o limite de infrações for atingido, a simulação é abortada e um aviso é registado no log e exibido no console*/
    if (threshold_reached) {
        fprintf(log, "\n[SIMULATION ABORTED] The simulation has been aborted due to threshold violation!\n Number of Violations=%d\n Threshold=%d\n", *violation_count, rules.violation_threshold);
        fflush(log);
        printf("\n[SIMULATION] The threshold limit has been reached (%d)\n", *violation_count);
        return 1;
    }

    return 0;
}

/* Verifica se existe combustivel suficiente para realizar a viagem */
int check_fuel(const FlightPlan *plan) {
    if (!plan) return 0;

    double fuel_needed = 0.0;

    for (int i = 0; i < plan->numSegments; i++) {
        const FlightSegment *seg = &plan->segments[i];

        /* Calcula a distância entre o ponto de partida e o ponto de chegada do segmento usando a fórmula de Haversine*/
        double dist = calculate_kilometers(seg->start.latitude,  seg->start.longitude, seg->end.latitude,    seg->end.longitude);

        /* Determina a taxa de consumo de combustível com base no modo de voo (climb, cruise, descend) e calcula o combustível necessário para o segmento*/
        double rate;
        if(strcmp(seg->mode, "climb")   == 0) {
            rate = FUEL_CLIMB_KG_PER_KM;
        }
        else if (strcmp(seg->mode, "descend") == 0){
            rate = FUEL_DESCEND_KG_PER_KM;
        }
        else{
            rate = FUEL_CRUISE_KG_PER_KM;
        }

        fuel_needed += dist * rate;
    }

    printf("[FUEL] Flight %d: needs ~%.0f kg, has %.0f kg\n", plan->id, fuel_needed, plan->fuelQuantity);

    /* Compara o combustível necessário para a viagem com o combustível disponível no plano de voo*/
    if (plan->fuelQuantity < fuel_needed) {
        return 0;
    }

    return 1;
}