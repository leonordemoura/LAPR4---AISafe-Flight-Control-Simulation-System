#include <stdio.h>
#include <time.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include "reportGeneration.h"

#define LOG_FILE "log/violations.log"
#define REPORTS_DIR "reports"

void clear_violations_log() {
    mkdir(REPORTS_DIR, 0755);
    mkdir("log", 0755);
    FILE *f = fopen(LOG_FILE, "w");
    if (f) fclose(f);
}

void log_live_violations(int step, SafetyViolationEvent violations[], int num_violations) {
    if (num_violations <= 0) return;

    FILE *f = fopen(LOG_FILE, "a");
    if (!f) {
        perror("[ERROR] Failed to open log/violations.log");
        return;
    }

    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    
    fprintf(f, "=================================================\n");
    fprintf(f, "[%02d-%02d-%04d %02d:%02d:%02d] STEP %d: %d NEW VIOLATION(S) DETECTED\n",
            t->tm_mday, t->tm_mon + 1, t->tm_year + 1900,
            t->tm_hour, t->tm_min, t->tm_sec,
            step, num_violations);
    fprintf(f, "=================================================\n");

    for (int i = 0; i < num_violations; i++) {
        fprintf(f, "  -> Violation #%d (Global ID: %d):\n", i + 1, violations[i].global_id);
        fprintf(f, "     Conflict between Flight %d and Flight %d\n", violations[i].flightA, violations[i].flightB);
    }
    fprintf(f, "\n");
    
    fclose(f);
}

int generate_report(
    FlightHistory         *history,
    int                    number_flights,
    SafetyViolationEvent  *events,
    int                    num_events,
    int                    threshold,
    int                    total_violation_count,
    int                    early_stop
) {
    char filename[256];
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    
    mkdir(REPORTS_DIR, 0755);
    
    strftime(filename, sizeof(filename), REPORTS_DIR "/report_%Y-%m-%d_%H-%M-%S.txt", t);
    
    FILE *f = fopen(filename, "w");
    if (!f) {
        perror("[ERROR] Failed to create report file");
        return -1;
    }

    fprintf(f, "========= SIMULATION REPORT =========\n");
    fprintf(f, "Total Flights: %d\n", number_flights);
    fprintf(f, "Total Violations: %d\n", total_violation_count);
    fprintf(f, "Violation Threshold: %d\n", threshold);
    fprintf(f, "\n-----------------------------------\n\n");

    fprintf(f, "FLIGHT HISTORIES:\n");
    for (int i = 0; i < number_flights; i++) {
        double score = 100.0;
        if (history[i].count > 0) {
            score = ((double)(history[i].count - history[i].violation_count) / history[i].count) * 100.0;
        }

        fprintf(f, "  Flight %d (Total Positions: %d, Violations: %d, Performance Score: %.2f%%):\n",
                history[i].flightId, history[i].count, history[i].violation_count, score);
        for (int j = 0; j < history[i].count; j++) {
            fprintf(f, "    Pos: (%f, %f)  Alt: %f  Vel: (%f, %f, %f)\n",
                    history[i].positions[j].x, history[i].positions[j].y, history[i].positions[j].altitude,
                    history[i].positions[j].vx, history[i].positions[j].vy, history[i].positions[j].vz);
        }
        fprintf(f, "\n");
    }

    fprintf(f, "-----------------------------------\n\n");

    if (total_violation_count > 0) {
        fprintf(f, "SAFETY VIOLATIONS:\n");
        if (num_events > 0) {
            fprintf(f, "(Showing last %d detected violations. Full list in %s)\n\n", num_events, LOG_FILE);
            for (int i = 0; i < num_events && i < MAX_VIOLATIONS; i++) {
                time_t t_sec = (time_t)events[i].timestamp_sec;
                struct tm *tm_info = localtime(&t_sec);
                char time_buf[20];
                if (tm_info) {
                    strftime(time_buf, sizeof(time_buf), "%H:%M", tm_info);
                } else {
                    strcpy(time_buf, "00:00");
                }

                fprintf(f, "  Violation #%d (Global ID: %d):\n", i + 1, events[i].global_id);
                fprintf(f, "    Time: %s\n", time_buf);
                fprintf(f, "    Flight A: %d\n", events[i].flightA);
                fprintf(f, "      Position: (%f, %f)  Altitude: %f\n", events[i].posA.x, events[i].posA.y, events[i].posA.altitude);
                fprintf(f, "      Velocity: (%f, %f, %f)\n", events[i].posA.vx, events[i].posA.vy, events[i].posA.vz);
                fprintf(f, "    Flight B: %d\n", events[i].flightB);
                fprintf(f, "      Position: (%f, %f)  Altitude: %f\n", events[i].posB.x, events[i].posB.y, events[i].posB.altitude);
                fprintf(f, "      Velocity: (%f, %f, %f)\n", events[i].posB.vx, events[i].posB.vy, events[i].posB.vz);
                fprintf(f, "    Distance: Horizontal=%f Vertical=%f\n", events[i].horizontal_distance, events[i].vertical_distance);
                fprintf(f, "\n");
            }
        } else {
            fprintf(f, "No violations in current buffer. See %s for the complete history.\n", LOG_FILE);
        }
    }

    fprintf(f, "-----------------------------------\n\n");

    fprintf(f, "VERDICT:\n");
    if (total_violation_count >= threshold && early_stop != 0) {
        fprintf(f, "SIMULATION FAILED (Threshold reached)\n");
    } else if (early_stop == 0) {
        fprintf(f, "SIMULATION ABORTED (Unexpected error)\n");
    } else {
        fprintf(f, "SIMULATION SUCCEEDED\n");
    }

    fprintf(f, "\n========= END OF REPORT =========\n");
    fclose(f);
    
    printf("[REPORT] Final report generated: %s\n", filename);
    return 0;
}

void generate_report_fuel_failure(int number_flights) {
    char filename[256];
    time_t now = time(NULL);
    struct tm *t = localtime(&now);
    
    mkdir(REPORTS_DIR, 0755);
    strftime(filename, sizeof(filename), REPORTS_DIR "/report_fuel_fail_%Y-%m-%d_%H-%M-%S.txt", t);
    
    FILE *f = fopen(filename, "w");
    if (f) {
        fprintf(f, "========= SIMULATION REPORT (FUEL FAILURE) =========\n");
        fprintf(f, "Total Flights attempted: %d\n", number_flights);
        fprintf(f, "VERDICT: SIMULATION ABORTED (Insufficient fuel)\n");
        fprintf(f, "========= END OF REPORT =========\n");
        fclose(f);
    }
}
