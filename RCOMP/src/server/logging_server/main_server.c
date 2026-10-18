#include "logging_server.h"
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

static void *udp_thread(void *arg) {
    (void)arg;
    udp_receiver_run();
    return NULL;
}

static void *http_thread(void *arg) {
    (void)arg;
    http_server_run();
    return NULL;
}

int main(void) {
    printf("=== Remote Accesses Logging Server ===\n");
    printf("UDP port : %d\n", UDP_PORT);
    printf("HTTP port: %d\n", HTTP_PORT);

    repository_init();

    pthread_t t_udp, t_http;

    if (pthread_create(&t_udp, NULL, udp_thread, NULL) != 0) {
        perror("pthread_create UDP"); return 1;
    }
    if (pthread_create(&t_http, NULL, http_thread, NULL) != 0) {
        perror("pthread_create HTTP"); return 1;
    }

    pthread_join(t_udp, NULL);
    pthread_join(t_http, NULL);

    return 0;
}