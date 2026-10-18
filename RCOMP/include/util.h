#ifndef UTIL_H
#define UTIL_H

#include "protocol.h"

#define RESPONSE_BUFFER 16384
#define COMMAND_BUFFER 8192
#define LINE_BUFFER 512

typedef struct {
    socket_t socket;
    int connected;
    int authenticated;
    char username[128];
    char host[128];
    int port;
} ClientSession;

void trim_newline(char *line);
void print_header(const char *title);
void read_line(const char *prompt, char *buffer, size_t size);
int send_and_receive(ClientSession *session, const char *command, char *response,size_t response_size);
int response_is_ok(const char *response);
void print_response(const char *response);
int connect_server(ClientSession *session);
void disconnect_server(ClientSession *session);
void do_login(ClientSession *session, const char *role_hint);
void do_logout(ClientSession *session) ;

#endif