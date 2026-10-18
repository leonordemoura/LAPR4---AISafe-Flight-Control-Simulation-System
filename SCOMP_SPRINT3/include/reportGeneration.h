#ifndef REPORT_GENERATION_H
#define REPORT_GENERATION_H

#include "common.h"

void clear_violations_log();
void log_live_violations(int step, SafetyViolationEvent violations[], int num_violations);

int generate_report(FlightHistory *history, int number_flights, SafetyViolationEvent *events, int num_events, int threshold, int total_violation_count, int early_stop);

void generate_report_fuel_failure(int number_flights);

#endif
