#include <stdio.h>
#include <stdlib.h>
#include "controller.h"
#include "common.h"
#include "config_loader.h"

#define SIM_CFG        "config/simulation.cfg"
#define SAFETY_CFG     "config/safety_rules.cfg"

void show_menu() {
    printf("\n==== MENU ====\n");
    printf("1 - Start Simulation\n");
    printf("0 - Exit\n");
    printf("Option: ");
}

void print_flights(FlightPlan plans[], int numFlights) {
    for (int i = 0; i < numFlights; i++) {
        printf("\n===== LOADED FLIGHT %d =====\n", i + 1);

        printf("ID: %d\n", plans[i].id);
        printf("Route: %s\n", plans[i].route);
        printf("Departure Date: %s\n", plans[i].departureDate);
        printf("Departure Time: %s\n", plans[i].departureTime);
        printf("Departure: %s\n", plans[i].departure);
        printf("Arrival: %s\n", plans[i].arrival);
        printf("Segments: %d\n", plans[i].numSegments);
        printf("\n\n");
    }
}

int main() {
    int option;

    do {
        show_menu();

        if (scanf("%d", &option) != 1) {
            printf("Invalid input\n");
            // Clear buffer
            while (getchar() != '\n');
            continue;
        }

        if (option == 0) {
            printf("Exiting...\n");
            break;
        }

        if (option != 1) {
            printf("Invalid option\n");
            continue;
        }

        SimulationConfig config;
        SafetyRules rules;
        FlightPlan plans[MAX_FLIGHTS];

        // ---------------- LOAD SIMULATION CONFIG ----------------
        if (!load_simulation_config(SIM_CFG, &config)) {
            printf("[ERROR] Failed to load simulation config.\n");
            continue;
        }

        // ---------------- Load Safety Rules ----------------

        if (!load_safety_rules(SAFETY_CFG, &rules)) {
            printf("[ERROR] Failed to load safety rules config.\n");
            continue;
        }

        char filePath[256];
        printf("Enter flight plan file path: ");
        if (scanf("%255s", filePath) != 1) {
            printf("[ERROR] Invalid file path input\n");
            continue;
        }

        // ---------------- LOAD JSON ----------------
        if (!load_flight_plans(filePath, &config, plans)) {
            printf("[ERROR] Failed to load JSON from %s\n", filePath);
            continue;
        }

        // ---------------- DEBUG ----------------
        print_flights(plans, config.numFlights);

        start_simulation(config, rules, plans);

    } while (option != 0);

    return 0;
}
