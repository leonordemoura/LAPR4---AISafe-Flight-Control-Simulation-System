#ifndef LOGGING_SERVER_H
#define LOGGING_SERVER_H

#include <time.h>

#define UDP_PORT        5000
#define HTTP_PORT       8080
#define MAX_EVENTS      500
#define MAX_ACTIVE      100
#define MAX_STR         256
#define BASE_FOLDER     "src/server/www"

/* Tipos de evento */
typedef enum {
    EVT_LOGIN_OK,
    EVT_LOGIN_FAIL,
    EVT_LOGOUT,
    EVT_DISCONNECT
} EventType;

/* Um evento de acesso */
typedef struct {
    EventType   type;
    char        timestamp[MAX_STR];
    char        username[MAX_STR];
    char        client_ip[MAX_STR];
    int         client_port;
    char        service[MAX_STR];   /* "US44", "US78" */
    time_t      raw_time;
} AccessEvent;

/* Sessão ativa (utilizador ainda ligado) */
typedef struct {
    char        username[MAX_STR];
    char        client_ip[MAX_STR];
    int         client_port;
    char        service[MAX_STR];
    char        login_time[MAX_STR];
    int         active;
} ActiveSession;

/* Repositório global (em memória) */
extern AccessEvent  events[MAX_EVENTS];
extern int          event_count;
extern ActiveSession active_sessions[MAX_ACTIVE];
extern int          active_count;

/* Mutex para acesso concorrente */
#include <pthread.h>
extern pthread_mutex_t repo_mutex;

/* Funções */
void repository_init(void);
void repository_add_event(AccessEvent *evt);
void repository_session_open(const char *username, const char *ip, int port, const char *service, const char *timestamp);
void repository_session_close(const char *username, const char *ip, int port, const char *service);

void udp_receiver_run(void);   /* corre em thread separada */
void http_server_run(void);    /* corre em thread separada */

#endif