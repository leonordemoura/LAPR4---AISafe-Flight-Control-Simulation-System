#ifndef CONTROLLER_H
#define CONTROLLER_H

#include "common.h"

int validate_config(SimulationConfig config);
int validate_areaBounds(const FlightPlan *plans, SimulationConfig config);
int validate_plan(const FlightPlan *plan);
int validate_rules(SafetyRules rules);
void start_simulation(SimulationConfig config, SafetyRules rules,const FlightPlan plans[]);

#endif