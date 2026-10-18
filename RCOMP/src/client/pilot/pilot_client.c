#include "protocol.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "util.h"



static void do_register_flight_plan(ClientSession *session) {
    char airlineCode[16];
    char flightNumberStr[16];
    char suffix[16];
    char aircraftRegistration[32];
    char pilotId[32];
    char departureDateTime[64];
    char arrivalDateTime[64];
    char fuelQuantityStr[32];
    char fuelUnit[16];
    char dslContent[4096];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("Login required.\n");
        return;
    }

    read_line("Airline code (IATA): ", airlineCode, sizeof(airlineCode));
    read_line("Flight number: ", flightNumberStr, sizeof(flightNumberStr));
    read_line("Operational suffix (blank if none): ", suffix, sizeof(suffix));
    read_line("Aircraft registration: ", aircraftRegistration, sizeof(aircraftRegistration));
    read_line("Pilot ID (certification id, e.g. P-001): ", pilotId, sizeof(pilotId));
    read_line("Departure date/time (yyyy-MM-ddTHH:mm): ", departureDateTime, sizeof(departureDateTime));
    read_line("Arrival date/time (yyyy-MM-ddTHH:mm, blank = departure + 1h): ",
              arrivalDateTime, sizeof(arrivalDateTime));
    read_line("Fuel quantity: ", fuelQuantityStr, sizeof(fuelQuantityStr));
    read_line("Fuel unit (KG/L): ", fuelUnit, sizeof(fuelUnit));
    read_line("DSL content (single line, no ';'): ", dslContent, sizeof(dslContent));

    snprintf(command, sizeof(command),
             "REGISTER_FLIGHT_PLAN;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s",
             airlineCode, flightNumberStr, suffix, aircraftRegistration, pilotId,
             departureDateTime, fuelQuantityStr, fuelUnit, dslContent, arrivalDateTime);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_import_flight_plan_file(ClientSession *session) {
    char airlineCode[16];
    char flightNumberStr[16];
    char suffix[16];
    char filePath[1024];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("Login required.\n");
        return;
    }

    read_line("Airline code (IATA): ", airlineCode, sizeof(airlineCode));
    read_line("Flight number: ", flightNumberStr, sizeof(flightNumberStr));
    read_line("Operational suffix (blank if none): ", suffix, sizeof(suffix));
    read_line("File path (on server host): ", filePath, sizeof(filePath));

    snprintf(command, sizeof(command),
             "IMPORT_FLIGHT_PLAN_FILE;%s;%s;%s;%s",
             airlineCode, flightNumberStr, suffix, filePath);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void print_menu(void) {
    printf("\n=== US086 Pilot Remote Client ===\n");
    printf("1. Login\n");
    printf("2. Register flight plan\n");
    printf("3. Import flight plan file\n");
    printf("4. Logout\n");
    printf("0. Exit\n");
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

    printf("Target server: %s:%d\n", session.host, session.port);

    for (;;) {
        print_menu();
        read_line("Option: ", choice, sizeof(choice));

        switch (choice[0]) {
            case '1':
                do_login(&session, "PILOT");
                break;
            case '2':
                do_register_flight_plan(&session);
                break;
            case '3':
                do_import_flight_plan_file(&session);
                break;
            case '4':
                do_logout(&session);
                break;
            case '0':
                if (session.authenticated) {
                    do_logout(&session);
                } else {
                    disconnect_server(&session);
                }
                protocol_cleanup();
                return 0;
            default:
                printf("Invalid option.\n");
                break;
        }
    }
}

