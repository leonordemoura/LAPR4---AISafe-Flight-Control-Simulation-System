#include "logging_server.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <time.h>
#include <arpa/inet.h>

/* Parse de uma linha UDP:
   TIMESTAMP;USERNAME;CLIENT_IP;CLIENT_PORT;SERVICE;EVENTO */
static int parse_datagram(const char *buf, AccessEvent *evt) {
    char tmp[1024];
    strncpy(tmp, buf, sizeof(tmp) - 1);

    char *ts      = strtok(tmp,  ";");
    char *user    = strtok(NULL, ";");
    char *ip      = strtok(NULL, ";");
    char *port_s  = strtok(NULL, ";");
    char *service = strtok(NULL, ";");
    char *type_s  = strtok(NULL, ";\n");

    if (!type_s || !ts || !user || !ip || !port_s || !service)
        return -1;

    if      (strcmp(type_s, "LOGIN_SUCCESS") == 0)
        evt->type = EVT_LOGIN_OK;

    else if (strcmp(type_s, "LOGIN_FAILED") == 0)
        evt->type = EVT_LOGIN_FAIL;

    else if (strcmp(type_s, "LOGOUT") == 0)
        evt->type = EVT_LOGOUT;

    else if (strcmp(type_s, "DISCONNECT") == 0)
        evt->type = EVT_DISCONNECT;

    else
        return -1;

    strncpy(evt->timestamp, ts, MAX_STR - 1);
    evt->timestamp[MAX_STR - 1] = '\0';

    strncpy(evt->username, user, MAX_STR - 1);
    evt->username[MAX_STR - 1] = '\0';

    strncpy(evt->client_ip, ip, MAX_STR - 1);
    evt->client_ip[MAX_STR - 1] = '\0';

    strncpy(evt->service, service, MAX_STR - 1);
    evt->service[MAX_STR - 1] = '\0';

    evt->client_port = atoi(port_s);
    evt->raw_time    = time(NULL);

    return 0;
}

void udp_receiver_run(void) {
    struct addrinfo hints, *res;
    int sock;
    char buf[1024];
    char port_str[16];

    memset(&hints, 0, sizeof(hints));
    hints.ai_family   = AF_INET6;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_flags    = AI_PASSIVE;

    snprintf(port_str, sizeof(port_str), "%d", UDP_PORT);
    if (getaddrinfo(NULL, port_str, &hints, &res) != 0) {
        perror("getaddrinfo UDP");
        return;
    }

    sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (sock < 0) { perror("socket UDP"); return; }

    /* Permite IPv4 e IPv6 */
    int no = 0;
    setsockopt(sock, IPPROTO_IPV6, IPV6_V6ONLY, &no, sizeof(no));

    if (bind(sock, res->ai_addr, res->ai_addrlen) < 0) {
        perror("bind UDP"); close(sock); return;
    }
    freeaddrinfo(res);

    printf("[UDP] A escutar na porta %d\n", UDP_PORT);

    struct sockaddr_storage from;
    socklen_t from_len;

    while (1) {
        from_len = sizeof(from);
        int n = recvfrom(sock, buf, sizeof(buf)-1, 0,
                         (struct sockaddr *)&from, &from_len);
        if (n < 0) continue;
        buf[n] = '\0';

        printf("[UDP] Recebido: %s\n", buf);

        AccessEvent evt;
        memset(&evt, 0, sizeof(evt));
        if (parse_datagram(buf, &evt) != 0) {
            fprintf(stderr, "[UDP] Datagrama inválido: %s\n", buf);
            continue;
        }

        repository_add_event(&evt);

        if (evt.type == EVT_LOGIN_OK) {
            repository_session_open(evt.username, evt.client_ip,
                                    evt.client_port, evt.service,
                                    evt.timestamp);
        } else if (evt.type == EVT_LOGOUT || evt.type == EVT_DISCONNECT) {
            repository_session_close(evt.username, evt.client_ip,
                                     evt.client_port, evt.service);
        }
    }
    close(sock);
}