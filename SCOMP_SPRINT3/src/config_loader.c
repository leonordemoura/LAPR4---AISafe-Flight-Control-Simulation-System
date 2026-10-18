#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "config_loader.h"
#include "common.h"

#define MAX_LINE 256

/* Trims leading and trailing whitespace from a string in place. */
static void trim(char *s) {
    char *start = s;
    while (*start == ' ' || *start == '\t') start++;
    if (start != s) memmove(s, start, strlen(start) + 1);

    char *end = s + strlen(s) - 1;
    while (end >= s && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r'))
        *end-- = '\0';
}

/* Splits a line into key and value based on the first '=' character. */
static int split_kv(char *line, char *key, char *value) {
    char *eq = strchr(line, '=');
    if (!eq) return 0;

    *eq = '\0';
    strncpy(key,   line,    MAX_LINE - 1); key[MAX_LINE - 1]   = '\0';
    strncpy(value, eq + 1,  MAX_LINE - 1); value[MAX_LINE - 1] = '\0';
    trim(key);
    trim(value);
    return 1;
}

/* Loads safety rules from a configuration file. */
int load_safety_rules(const char *filepath, SafetyRules *rules) {
    FILE *f = fopen(filepath, "r");
    if (!f) {
        fprintf(stderr, "[CONFIG] Cannot open safety rules config: %s\n", filepath);
        return 0;
    }

    rules->horizontal_separation = 0.0;
    rules->vertical_separation     = 0.0;
    rules->violation_threshold     = 0;

    char line[MAX_LINE], key[MAX_LINE], value[MAX_LINE];

    while (fgets(line, sizeof(line), f)) {
        trim(line);
        if (line[0] == '#' || line[0] == '\0') continue;

        if (!split_kv(line, key, value)) continue;

        if (strcmp(key, "horizontal_separation") == 0)
            rules->horizontal_separation = atof(value);
        else if (strcmp(key, "vertical_separation") == 0)
            rules->vertical_separation = atof(value);
        else if (strcmp(key, "violation_threshold") == 0)
            rules->violation_threshold = atoi(value);
    }

    fclose(f);
    return 1;
}

/* Loads simulation configuration from a configuration file. */
int load_simulation_config(const char *filepath, SimulationConfig *config) {
    FILE *f = fopen(filepath, "r");
    if (!f) {
        fprintf(stderr, "[CONFIG] Cannot open simulation config: %s\n", filepath);
        return 0;
    }

    char line[MAX_LINE], key[MAX_LINE], value[MAX_LINE];

    while (fgets(line, sizeof(line), f)) {
        trim(line);
        if (line[0] == '#' || line[0] == '\0') continue;

        if (!split_kv(line, key, value)) continue;

        if (strcmp(key, "startTime") == 0)
            config->startTime = atoi(value);
        else if (strcmp(key, "endTime") == 0)
            config->endTime = atoi(value);
        else if (strcmp(key, "minX") == 0)
            config->minX = atof(value);
        else if (strcmp(key, "maxX") == 0)
            config->maxX = atof(value);
        else if (strcmp(key, "minY") == 0)
            config->minY = atof(value);
        else if (strcmp(key, "maxY") == 0)
            config->maxY = atof(value);
        else if (strcmp(key, "weatherCondition") == 0)
            config->weatherCondition = atoi(value);
        else if (strcmp(key, "simulationSpeed") == 0)
            config->simulationSpeed = atof(value);
        else if (strcmp(key, "updateInterval") == 0)
            config->updateInterval = atof(value);
    }

    fclose(f);
    return 1;
}

