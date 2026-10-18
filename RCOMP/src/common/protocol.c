#include "protocol.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifdef _WIN32
#include <ws2tcpip.h>
#pragma comment(lib, "ws2_32.lib")
#else
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/socket.h>
#include <unistd.h>
#endif

static int recv_all(socket_t sock, char *buffer, size_t length) {
    size_t received = 0;
    while (received < length) {
#ifdef _WIN32
        int n = recv(sock, buffer + received, (int)(length - received), 0);
#else
        ssize_t n = recv(sock, buffer + received, length - received, 0);
#endif
        if (n <= 0) {
            return -1;
        }
        received += (size_t)n;
    }
    return 0;
}

int protocol_init(void) {
#ifdef _WIN32
    WSADATA wsa;
    return WSAStartup(MAKEWORD(2, 2), &wsa) == 0 ? 0 : -1;
#else
    return 0;
#endif
}

void protocol_cleanup(void) {
#ifdef _WIN32
    WSACleanup();
#endif
}

socket_t protocol_connect(const char *host, int port) {
    char port_str[16];
    struct addrinfo hints;
    struct addrinfo *result = NULL;
    socket_t sock = (socket_t)-1;

    snprintf(port_str, sizeof(port_str), "%d", port);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    if (getaddrinfo(host, port_str, &hints, &result) != 0) {
        return (socket_t)-1;
    }

    sock = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
    if (sock == (socket_t)-1) {
        freeaddrinfo(result);
        return (socket_t)-1;
    }

    if (connect(sock, result->ai_addr, (int)result->ai_addrlen) != 0) {
        protocol_close(sock);
        freeaddrinfo(result);
        return (socket_t)-1;
    }

    freeaddrinfo(result);
    return sock;
}

void protocol_close(socket_t sock) {
    if (sock == (socket_t)-1) {
        return;
    }
#ifdef _WIN32
    closesocket(sock);
#else
    close(sock);
#endif
}

int protocol_send_message(socket_t sock, const char *payload) {
    if (!payload) {
        return -1;
    }

    const uint32_t length = htonl((uint32_t)strlen(payload));
    if (send(sock, (const char *)&length, sizeof(length), 0) != (int)sizeof(length)) {
        return -1;
    }
    if (send(sock, payload, (int)strlen(payload), 0) != (int)strlen(payload)) {
        return -1;
    }
    return 0;
}

int protocol_receive_message(socket_t sock, char *buffer, size_t buffer_size) {
    uint32_t net_length = 0;
    uint32_t length = 0;

    if (recv_all(sock, (char *)&net_length, sizeof(net_length)) != 0) {
        return -1;
    }

    length = ntohl(net_length);
    if (length == 0 || length >= buffer_size) {
        return -1;
    }

    if (recv_all(sock, buffer, length) != 0) {
        return -1;
    }

    buffer[length] = '\0';
    return 0;
}
