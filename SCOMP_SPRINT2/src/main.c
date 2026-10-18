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
    printf("2 - Start Simulation - Same Flights\n");
    printf("3 - Start Simulation - 3 Different Flights (Fuel Deficit)\n");
    printf("4 - Start Simulation - 2 Different Flights (Overlay)\n");
    printf("5 - Start Simulation - 50 Flights\n");
    printf("0 - Exit\n");
    printf("Option: ");
}

void print_flights(FlightPlan plans[], int numFlights) {
    for (int i = 0; i < numFlights; i++) {
        printf("\n===== LOADED FLIGHT %d =====\n", i + 1);

        printf("ID: %d\n", plans[i].id);
        printf("Route: %s\n", plans[i].route);
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
            exit(1);
        }

        SimulationConfig config;
        SafetyRules rules;
        FlightPlan plans[MAX_FLIGHTS];

        // ---------------- LOAD SIMULATION CONFIG ----------------
        if (!load_simulation_config(SIM_CFG, &config)) {
            printf("[ERROR] Failed to load simulation config.\n");
            break;
        }

        // ---------------- Load Safety Rules ----------------

        if (!load_safety_rules(SAFETY_CFG, &rules)) {
            printf("[ERROR] Failed to load safety rules config.\n");
            break;
        }

        switch (option) {
            case 1: {
                // ---------------- LOAD JSON ----------------
                if (!load_flight_plans(
                        "src/Flight_Plan_v0c.json",
                        &config,
                        plans)) {

                    printf("[ERROR] Failed to load JSON\n");
                    return 1;
                }

                // ---------------- DEBUG ----------------
                print_flights( plans,  1);

                start_simulation(config, rules, plans);
                break;
            }
            case 2: {
                // ---------------- LOAD JSON ----------------
                if (!load_flight_plans(
                        "src/Flight_Plan_v2.json",
                        &config,
                        plans)) {

                    printf("[ERROR] Failed to load JSON\n");
                    return 1;
                }

                // ---------------- DEBUG ----------------
                    print_flights( plans,  2);

                start_simulation(config, rules, plans);
                break;
            }
            case 3: {
                // ---------------- LOAD JSON ----------------
                if (!load_flight_plans(
                        "src/Flight_Plan_v3.json",
                        &config,
                        plans)) {

                    printf("[ERROR] Failed to load JSON\n");
                    return 1;
                }

                // ---------------- DEBUG ----------------
                print_flights( plans,  3);


                start_simulation(config, rules, plans);
                break;
            }

            case 4: {

                // ---------------- LOAD JSON ----------------
                if (!load_flight_plans(
                        "src/Flight_Plan_v4.json",
                        &config,
                        plans)) {

                    printf("[ERROR] Failed to load JSON\n");
                    return 1;
                }

                // ---------------- DEBUG ----------------
                print_flights( plans,  2);



                start_simulation(config, rules, plans);
                break;
            }
            case 5: {

                // ---------------- LOAD JSON ----------------
                if (!load_flight_plans(
                        "src/Flight_Plan_v5.json",
                        &config,
                        plans)) {

                    printf("[ERROR] Failed to load JSON\n");
                    return 1;
                }

                // ---------------- DEBUG ----------------
                print_flights( plans,  50);

                start_simulation(config, rules, plans);
                break;
            }
            case 0:
                printf("Exiting...\n");
                break;
            default:
                printf("Invalid option\n");
        }

    } while (option != 0);

    return 0;
}
