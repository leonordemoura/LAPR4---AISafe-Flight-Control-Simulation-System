#include "protocol.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "util.h"

static void do_add_aircraft(ClientSession *session) {
    char registration[32], modelId[32], engines[128], countryName[64], iataCode[16], status[16];
    char first[16], business[16], economy[16], year[16], countryCode[16];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Add Aircraft to Fleet");

    read_line("Registration:", registration, sizeof(registration));
    read_line("Model ID:", modelId, sizeof(modelId));
    read_line("Engines (comma separated):", engines, sizeof(engines));
    read_line("First class seats:", first, sizeof(first));
    read_line("Business class seats:", business, sizeof(business));
    read_line("Economy class seats:", economy, sizeof(economy));
    read_line("Registration year:", year, sizeof(year));
    read_line("Country name:", countryName, sizeof(countryName));
    read_line("Country numeric code:", countryCode, sizeof(countryCode));
    read_line("Status (ACTIVE/DECOMMISSIONED):", status, sizeof(status));
    read_line("Company IATA code:", iataCode, sizeof(iataCode));

    snprintf(command, sizeof(command),
             "ADD_AIRCRAFT;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
             registration, modelId, engines, first, business, economy, year, countryName, countryCode, status, iataCode);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_decommission_aircraft(ClientSession *session) {
    char registration[32];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Decommission Aircraft");
    read_line("Registration:", registration, sizeof(registration));
    snprintf(command, sizeof(command), "DECOMMISSION_AIRCRAFT;%s", registration);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_list_fleet(ClientSession *session) {
    char iataCode[16];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("List Company Fleet");
    read_line("Company IATA code:", iataCode, sizeof(iataCode));
    snprintf(command, sizeof(command), "LIST_FLEET;%s", iataCode);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_register_route(ClientSession *session) {
    char routeName[16], origin[16], dest[16];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Register Flight Route");
    read_line("Route name (e.g. TP123):", routeName, sizeof(routeName));
    read_line("Origin IATA code:", origin, sizeof(origin));
    read_line("Destination IATA code:", dest, sizeof(dest));

    snprintf(command, sizeof(command), "REGISTER_FLIGHT_ROUTE;%s;%s;%s", routeName, origin, dest);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_deactivate_route(ClientSession *session) {
    char routeName[16], date[64];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Deactivate Flight Route");
    read_line("Route name:", routeName, sizeof(routeName));
    read_line("Date (yyyy-MM-ddTHH:mm:ss):", date, sizeof(date));

    snprintf(command, sizeof(command), "DEACTIVATE_FLIGHT_ROUTE;%s;%s", routeName, date);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_add_pilot(ClientSession *session) {
    char pilotId[32], email[128], certs[1024];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Add Pilot to Company");
    read_line("Pilot ID:", pilotId, sizeof(pilotId));
    read_line("Email:", email, sizeof(email));
    read_line("Certs (ModelID,YYYY-MM-DD;...):", certs, sizeof(certs));
    
    snprintf(command, sizeof(command), "ADD_PILOT;%s;%s;%s", pilotId, email, certs);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_list_roster(ClientSession *session) {
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("List Pilot Roster");
    if (send_and_receive(session, "LIST_PILOT_ROSTER", response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_remove_pilot(ClientSession *session) {
    char pilotId[32];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("\n[!] Authentication required.\n");
        return;
    }

    print_header("Remove Pilot");
    read_line("Pilot ID:", pilotId, sizeof(pilotId));
    snprintf(command, sizeof(command), "REMOVE_PILOT;%s", pilotId);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void print_menu(ClientSession *session) {
    char title[256];
    if (session->authenticated) {
        snprintf(title, sizeof(title), "ATC Collaborator Management [ @%s ]", session->username);
    } else {
        snprintf(title, sizeof(title), "ATC Collaborator Management [ Anonymous ]");
    }
    print_header(title);
    printf("  1. Login\n");
    printf("  2. Add Aircraft\n");
    printf("  3. Decommission Aircraft\n");
    printf("  4. List Fleet\n");
    printf("  5. Register Flight Route\n");
    printf("  6. Deactivate Flight Route\n");
    printf("  7. Add Pilot to Company\n");
    printf("  8. List Pilot Roster\n");
    printf("  9. Remove Pilot\n");
    printf("  10. Logout\n");
    printf("  0. Exit\n");
    printf("--------------------------------------------------------------------------------\n");
}

int main(int argc, char *argv[]) {
    ClientSession session;
    char choice[LINE_BUFFER];

    memset(&session, 0, sizeof(session));
    strncpy(session.host, "127.0.0.1", sizeof(session.host) - 1);
    session.port = 9000;
    session.socket = (socket_t) -1;

    if (argc >= 2) {
        strncpy(session.host, argv[1], sizeof(session.host) - 1);
    }
    if (argc >= 3) {
        session.port = atoi(argv[2]);
    }

    if (protocol_init() != 0) {
        fprintf(stderr, "Failed to initialize network stack.\n");
        return 1;
    }

    for (;;) {
        print_menu(&session);
        read_line("Choose an option:", choice, sizeof(choice));

        int opt = atoi(choice);
        switch (opt) {
            case 1: do_login(&session, "COLLABORATOR"); break;
            case 2: do_add_aircraft(&session); break;
            case 3: do_decommission_aircraft(&session); break;
            case 4: do_list_fleet(&session); break;
            case 5: do_register_route(&session); break;
            case 6: do_deactivate_route(&session); break;
            case 7: do_add_pilot(&session); break;
            case 8: do_list_roster(&session); break;
            case 9: do_remove_pilot(&session); break;
            case 10: do_logout(&session); break;
            case 0:
                printf("\nExiting...\n");
                if (session.connected) {
                    disconnect_server(&session);
                }
                protocol_cleanup();
                return 0;
            default:
                if (choice[0] != '\0') printf("\n[!] Invalid option.\n");
                break;
        }
    }
}
