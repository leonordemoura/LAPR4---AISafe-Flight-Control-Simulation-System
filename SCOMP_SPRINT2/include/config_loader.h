#ifndef CONFIG_LOADER_H
#define CONFIG_LOADER_H

#include "common.h"

int load_safety_rules(const char *filepath, SafetyRules *rules);
int load_simulation_config(const char *filepath, SimulationConfig *config);

#endif
