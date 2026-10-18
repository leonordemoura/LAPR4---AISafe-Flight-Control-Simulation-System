#ifndef RCOMP_PROTOCOL_H
#define RCOMP_PROTOCOL_H

#include <stddef.h>

#ifdef _WIN32
#include <winsock2.h>
typedef SOCKET socket_t;
#else
typedef int socket_t;
#endif

int protocol_init(void);
void protocol_cleanup(void);

socket_t protocol_connect(const char *host, int port);
void protocol_close(socket_t sock);

int protocol_send_message(socket_t sock, const char *payload);
int protocol_receive_message(socket_t sock, char *buffer, size_t buffer_size);

#endif
