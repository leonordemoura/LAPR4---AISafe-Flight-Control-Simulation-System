#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "util.h"


void trim_newline(char *line) {
    if (!line) {
        return;
    }
    line[strcspn(line, "\r\n")] = '\0';
}

void print_header(const char *title) {
    printf("\n================================================================================\n");
    printf("  %s\n", title);
    printf("================================================================================\n");
}

void read_line(const char *prompt, char *buffer, size_t size) {
    printf("%-30s ", prompt);
    if (!fgets(buffer, (int) size, stdin)) {
        buffer[0] = '\0';
        return;
    }
    trim_newline(buffer);
}

int send_and_receive(ClientSession *session, const char *command, char *response, size_t response_size) {
    printf("\n[Network] Sending request...");
    fflush(stdout);
    if (protocol_send_message(session->socket, command) != 0) {
        printf(" [FAILED]\n");
        fprintf(stderr, "Error: Failed to send command.\n");
        return -1;
    }
    if (protocol_receive_message(session->socket, response, response_size) != 0) {
        printf(" [FAILED]\n");
        fprintf(stderr, "Error: Failed to receive response.\n");
        return -1;
    }
    printf(" [OK]\n");
    return 0;
}

int response_is_ok(const char *response) {
    return strncmp(response, "OK;", 3) == 0;
}

void print_response(const char *response) {
    printf("--------------------------------------------------------------------------------\n");
    if (response_is_ok(response)) {
        printf(" SUCCESS: %s\n", response + 3);
    } else {
        printf(" ERROR:   %s\n", response);
    }
    printf("--------------------------------------------------------------------------------\n");
}

int connect_server(ClientSession *session) {
    if (session->connected) {
        return 0;
    }
    printf("\n[Network] Connecting to %s:%d...", session->host, session->port);
    fflush(stdout);
    session->socket = protocol_connect(session->host, session->port);
    if (session->socket == (socket_t) -1) {
        printf(" [FAILED]\n");
        fprintf(stderr, "Error: Unable to connect.\n");
        return -1;
    }
    printf(" [OK]\n");
    session->connected = 1;
    return 0;
}

void disconnect_server(ClientSession *session) {
    if (!session->connected) {
        return;
    }
    printf("\n[Network] Disconnecting...");
    fflush(stdout);
    protocol_close(session->socket);
    session->socket = (socket_t) -1;
    session->connected = 0;
    session->authenticated = 0;
    session->username[0] = '\0';
    printf(" [OK]\n");
}

void do_login(ClientSession *session, const char *role_hint) {
    char username[128];
    char password[128];
    char command[COMMAND_BUFFER];
    char response[RESPONSE_BUFFER];

    print_header("Login Required");

    if (!session->connected && connect_server(session) != 0) {
        return;
    }

    read_line("Email:", username, sizeof(username));
    read_line("Password:", password, sizeof(password));
    if (role_hint != NULL && role_hint[0] != '\0') {
        snprintf(command, sizeof(command), "LOGIN;%s;%s;%s", username, password, role_hint);
    } else {
        snprintf(command, sizeof(command), "LOGIN;%s;%s", username, password);
    }

    if (send_and_receive(session, command, response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);

    if (response_is_ok(response)) {
        session->authenticated = 1;
        strncpy(session->username, username, sizeof(session->username) - 1);
        session->username[sizeof(session->username) - 1] = '\0';
    }
}

void do_logout(ClientSession *session) {
    char response[RESPONSE_BUFFER];
    if (!session->connected || !session->authenticated) {
        printf("\n[!] Not authenticated.\n");
        return;
    }
    
    print_header("Logging out...");
    if (send_and_receive(session, "LOGOUT", response, sizeof(response)) != 0) {
        return;
    }
    print_response(response);
    session->authenticated = 0;
    session->username[0] = '\0';
    disconnect_server(session);
}
