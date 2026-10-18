#include <stdio.h>
#include <time.h>
#include <sys/stat.h>

#include "common.h"
#include "util.h"

int generate_report(FlightHistory *history, int number_flights, SafetyViolationEvent *events, int threshold, int violation_count, int early_stop) {
    // Gerar nome do ficheiro com data/hora sem espaços
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    char fileName[64];
    strftime(fileName, sizeof(fileName), "reports/report_%Y-%m-%d_%H-%M-%S.txt", t);

    // Criar diretório "reports" se não existir
    mkdir("reports", 0755);
    FILE *report = fopen(fileName, "w");
    if (!report) {
        perror("[ERROR] Could not create report file");
        return -1;
    }

    fprintf(report, "========= SIMULATION REPORT =========\n");
    fprintf(report, "Total Flights: %d\n", number_flights);
    fprintf(report, "Total Violations: %d\n", violation_count);
    fprintf(report, "Violation Threshold: %d\n", threshold);
    fprintf(report, "\n-----------------------------------\n");

    fprintf(report, "\nFLIGHT HISTORIES:\n");
    for (int i = 0; i < number_flights; i++) {
        fprintf(report, "\nFlight %d:\n", history[i].positions[0].flightId);
        for (int j = 0; j < history[i].count; j++) {
            char time[6];
            turn_time_into_char(history[i].positions[j].timestamp / 60, time, sizeof(time));
            fprintf(report, " Time: %s Position: (%.2f, %.2f), Altitude: %.2f\n", time, history[i].positions[j].x, history[i].positions[j].y, history[i].positions[j].altitude);
       }
    }

    if(violation_count > 0){
        fprintf(report, "\n-----------------------------------\n");
        fprintf(report, "\nSAFETY VIOLATIONS:\n");
        for (int i = 0; i < violation_count; i++) {
            char time_str[6], time_flightB[6];
            turn_time_into_char(events[i].posA.timestamp / 60, time_str, sizeof(time_str));
            turn_time_into_char(events[i].posB.timestamp / 60, time_flightB, sizeof(time_flightB));
            fprintf(report, "Violation #%d:\n", i + 1);
            fprintf(report, "  Time: %s\n", time_str);
            fprintf(report, "  Horizontal Distance: %.4f, Vertical Distance: %.4f\n", events[i].horizontal_distance, events[i].vertical_distance);
            fprintf(report, "  Flight %d: Position: (%.2f, %.2f), Altitude: %.2f, Velocity Vector (%.2f, %.2f, %.2f) Time: %s\n", events[i].flightA, events[i].posA.x, events[i].posA.y, events[i].posA.altitude, events[i].velA.vx, events[i].velA.vy, events[i].velA.vz, time_str);
            fprintf(report, "  Flight %d: Position: (%.2f, %.2f), Altitude: %.2f, Velocity Vector (%.2f, %.2f, %.2f) Time: %s\n", events[i].flightB, events[i].posB.x, events[i].posB.y, events[i].posB.altitude, events[i].velB.vx, events[i].velB.vy, events[i].velB.vz, time_flightB);
        }
    }

    fprintf(report, "\n-----------------------------------\n");

    fprintf(report, "\nFLIGHT STATUS:\n");
    for(int i = 0; i < number_flights; i++){
        if(history[i].violation_count > 0){
            fprintf(report, "\nFlight %d:\n", history[i].positions[0].flightId);
            fprintf(report, "  Total Violations: %d\n", history[i].violation_count);
            /* Calcula a performance como a percentagem de posições sem violações em relação ao total de posições */
            int total = history[i].count;
            int violations = history[i].violation_count;
            double performance = ((double)(total - violations) / total) * 100.0;
            if (violation_count != threshold && !early_stop) {
                fprintf(report, "  Performance: %.2f\n\n", performance);
            }else{
                fprintf(report, "  Unable to calculate performance.\n\n");
            }
        }else{
            fprintf(report, "\nFlight %d:\n", history[i].positions[0].flightId);
            fprintf(report, "  No violations detected.\n");
            if(violation_count != threshold && !early_stop){
                fprintf(report, "  Performance: 100.00\n\n");
            }else{
                fprintf(report, "  Unable to calculate performance.\n\n");
            }
        }
    }

    if (violation_count >= threshold) {
        fprintf(report, "Simulation was aborted due to reaching the violation threshold.\n");
        fprintf(report, "SIMULATION FAILED :(");
    }else{
        fprintf(report, "Simulation completed successfully without reaching the violation threshold.\n");
        fprintf(report, "SIMULATION SUCCEEDED :)\n");
     }

    fprintf(report, "\n========= END OF REPORT =========\n");
    fclose(report);
    return 0;
}

void generate_report_validation_failure(const char *message) {
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    char fileName[64];
    strftime(fileName, sizeof(fileName), "reports/report_%Y-%m-%d_%H-%M-%S.txt", t);

    mkdir("reports", 0755);
    FILE *report = fopen(fileName, "w");
    if (!report) {
        perror("[ERROR] Could not create report file");
        return;
    }

    fprintf(report, "========= SIMULATION REPORT =========\n");
    fprintf(report, "%s\n\n", message);
    fprintf(report, "SIMULATION ABORTED :(\n");
    fprintf(report, "\n========= END OF REPORT =========\n");

    fclose(report);
}