#include "protocol.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "util.h"

static void do_register_weather(ClientSession *session) {
    char area[64];
    char start[64];
    char end[64];
    char source[64];
    char windSpeedUnit[16];
    char tempUnit[16];
    char pressureUnit[16];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];
    double minLat, maxLat, minLon, maxLon, minAlt, maxAlt;
    double windDir, windSpeed, temperature, pressure;

    if (!session->authenticated) {
        printf("Login required.\n");
        return;
    }

    read_line("Area code: ", area, sizeof(area));
    read_line("Start (yyyy-MM-ddTHH:mm): ", start, sizeof(start));
    read_line("End (yyyy-MM-ddTHH:mm): ", end, sizeof(end));
    read_line("Min latitude: ", command, sizeof(command));
    minLat = atof(command);
    read_line("Max latitude: ", command, sizeof(command));
    maxLat = atof(command);
    read_line("Min longitude: ", command, sizeof(command));
    minLon = atof(command);
    read_line("Max longitude: ", command, sizeof(command));
    maxLon = atof(command);
    read_line("Min altitude (m): ", command, sizeof(command));
    minAlt = atof(command);
    read_line("Max altitude (m): ", command, sizeof(command));
    maxAlt = atof(command);
    read_line("Weather source: ", source, sizeof(source));
    read_line("Wind direction (deg): ", command, sizeof(command));
    windDir = atof(command);
    read_line("Wind speed: ", command, sizeof(command));
    windSpeed = atof(command);
    read_line("Wind speed unit (MPS/KMH/KT): ", windSpeedUnit, sizeof(windSpeedUnit));
    read_line("Temperature: ", command, sizeof(command));
    temperature = atof(command);
    read_line("Temperature unit (C/F): ", tempUnit, sizeof(tempUnit));
    read_line("Pressure: ", command, sizeof(command));
    pressure = atof(command);
    read_line("Pressure unit (HPA/PA): ", pressureUnit, sizeof(pressureUnit));

    snprintf(command, sizeof(command),
             "REGISTER_WEATHER;%s;%s;%s;%.6f;%.6f;%.6f;%.6f;%.6f;%.6f;%s;%.2f;%.2f;%s;%.2f;%s;%.2f;%s",
             area, start, end, minLat, maxLat, minLon, maxLon, minAlt, maxAlt,
             source, windDir, windSpeed, windSpeedUnit, temperature, tempUnit, pressure, pressureUnit);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_consult_weather(ClientSession *session) {
    char area[64];
    char date[32];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("Login required.\n");
        return;
    }

    read_line("Area code: ", area, sizeof(area));
    read_line("Date (yyyy-MM-dd): ", date, sizeof(date));
    snprintf(command, sizeof(command), "CONSULT_WEATHER;%s;%s", area, date);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void do_import_bulk(ClientSession *session) {
    char filePath[512];
    char format[32];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    if (!session->authenticated) {
        printf("Login required.\n");
        return;
    }

    read_line("CSV file path: ", filePath, sizeof(filePath));
    read_line("Format (CSV): ", format, sizeof(format));
    snprintf(command, sizeof(command), "IMPORT_WEATHER_BULK;%s;%s", filePath, format);

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
}

static void print_menu(void) {
    printf("\n=== US044 Weather Person Remote Client ===\n");
    printf("1. Login\n");
    printf("2. Register weather data\n");
    printf("3. Consult weather data\n");
    printf("4. Import bulk weather data\n");
    printf("5. Logout\n");
    printf("0. Exit\n");
}

int main(int argc, char *argv[]) {
    ClientSession session;
    char choice[LINE_BUFFER];

    memset(&session, 0, sizeof(session));
    strncpy(session.host, "127.0.0.1", sizeof(session.host) - 1);
    session.port = 9000;
    session.socket = (socket_t)-1;

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
                do_login(&session, NULL);
                break;
            case '2':
                do_register_weather(&session);
                break;
            case '3':
                do_consult_weather(&session);
                break;
            case '4':
                do_import_bulk(&session);
                break;
            case '5':
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
