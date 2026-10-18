#ifndef US101_H
#define US101_H
#include "common.h"

int load_flight_plans(const char *filename, SimulationConfig *config, FlightPlan plans[]);
int validate_config(SimulationConfig config);
int validate_areaBounds(const FlightPlan *plans, SimulationConfig config);
int validate_plan(const FlightPlan *plan);
int validate_rules(SafetyRules rules);
void start_simulation(SimulationConfig config, SafetyRules rules,const FlightPlan plans[]);
int capture_violation(FILE *log, SimulationConfig config, SafetyRules rules, FlightHistory *history, const pid_t pids[], const int flight_active[], int *violation_count, SafetyViolationEvent *events);
int check_fuel(const FlightPlan *plan);
int generate_report(FlightHistory *history, int number_flights, SafetyViolationEvent *events, int threshold, int violation_count, int early_stop);
void generate_report_validation_failure(const char *message);
void run_us101();

#endif