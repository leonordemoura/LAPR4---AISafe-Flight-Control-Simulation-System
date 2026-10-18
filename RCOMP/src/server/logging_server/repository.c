#include "logging_server.h"
#include <string.h>
#include <stdio.h>

AccessEvent   events[MAX_EVENTS];
int           event_count = 0;
ActiveSession active_sessions[MAX_ACTIVE];
int           active_count = 0;
pthread_mutex_t repo_mutex = PTHREAD_MUTEX_INITIALIZER;

void repository_init(void) {
    memset(events, 0, sizeof(events));
    memset(active_sessions, 0, sizeof(active_sessions));
    event_count = 0;
    active_count = 0;
}

void repository_add_event(AccessEvent *evt) {
    pthread_mutex_lock(&repo_mutex);
    if (event_count < MAX_EVENTS) {
        events[event_count++] = *evt;
    } else {
        /* Circular: desloca tudo uma posição */
        memmove(&events[0], &events[1],
                sizeof(AccessEvent) * (MAX_EVENTS - 1));
        events[MAX_EVENTS - 1] = *evt;
    }
    pthread_mutex_unlock(&repo_mutex);
}

void repository_session_open(const char *username, const char *ip,
                              int port, const char *service,
                              const char *timestamp) {
    pthread_mutex_lock(&repo_mutex);
    /* Procura slot livre */
    for (int i = 0; i < MAX_ACTIVE; i++) {
        if (!active_sessions[i].active) {
            strncpy(active_sessions[i].username, username, MAX_STR - 1);
            active_sessions[i].username[MAX_STR - 1] = '\0';

            strncpy(active_sessions[i].client_ip, ip, MAX_STR - 1);
            active_sessions[i].client_ip[MAX_STR - 1] = '\0';

            strncpy(active_sessions[i].service, service, MAX_STR - 1);
            active_sessions[i].service[MAX_STR - 1] = '\0';

            strncpy(active_sessions[i].login_time, timestamp, MAX_STR - 1);
            active_sessions[i].login_time[MAX_STR - 1] = '\0';

            active_sessions[i].client_port = port;
            active_sessions[i].active = 1;
            if (i >= active_count) active_count = i + 1;
            break;
        }
    }
    pthread_mutex_unlock(&repo_mutex);
}

void repository_session_close(const char *username, const char *ip,
                               int port, const char *service) {
    pthread_mutex_lock(&repo_mutex);
    for (int i = 0; i < active_count; i++) {
        if (active_sessions[i].active &&
            strcmp(active_sessions[i].username, username) == 0 &&
            strcmp(active_sessions[i].client_ip, ip) == 0 &&
            active_sessions[i].client_port == port &&
            strcmp(active_sessions[i].service, service) == 0) {
            active_sessions[i].active = 0;
            break;
        }
    }
    pthread_mutex_unlock(&repo_mutex);
}