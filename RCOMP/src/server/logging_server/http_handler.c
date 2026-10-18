#include "logging_server.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netdb.h>
#include <signal.h>
#include <pthread.h>

/* ── Utilitários HTTP ─────────────────────────────── */

static void write_line_crlf(int sock, const char *line) {
    write(sock, line, strlen(line));
    write(sock, "\r\n", 2);
}

static void read_line_crlf(int sock, char *line, int max) {
    char *p = line;
    int n = 0;

    while (n < max - 1) {
        char c;
        if (read(sock, &c, 1) <= 0) break;

        if (c == '\n') {
            *p = '\0';
            return;
        }

        if (c != '\r') {
            *p++ = c;
            n++;
        }
    }

    *p = '\0';
}

/* ── HTTP header ─────────────────────────────── */

static void send_header(int sock, const char *status,
                        const char *content_type, int length) {
    char buf[256];

    snprintf(buf, sizeof(buf), "HTTP/1.1 %s", status);
    write_line_crlf(sock, buf);

    snprintf(buf, sizeof(buf), "Content-Type: %s", content_type);
    write_line_crlf(sock, buf);

    snprintf(buf, sizeof(buf), "Content-Length: %d", length);
    write_line_crlf(sock, buf);

    write_line_crlf(sock, "Connection: close");
    write_line_crlf(sock, "Access-Control-Allow-Origin: *");
    write_line_crlf(sock, "");
}

static void send_string(int sock, const char *status,
                        const char *ct, const char *body) {
    send_header(sock, status, ct, (int)strlen(body));
    write(sock, body, strlen(body));
}

/* ── Static files ─────────────────────────────── */

static void send_file(int sock, const char *filepath) {
    FILE *f = fopen(filepath, "rb");
    if (!f) {
        send_string(sock, "404 Not Found", "text/html",
                    "<h1>404 Not Found</h1>");
        return;
    }

    const char *ct = "text/html";
    const char *ext = strrchr(filepath, '.');

    if (ext) {
        if (!strcmp(ext, ".js"))  ct = "application/javascript";
        if (!strcmp(ext, ".css")) ct = "text/css";
        if (!strcmp(ext, ".png")) ct = "image/png";
    }

    fseek(f, 0, SEEK_END);
    long len = ftell(f);
    rewind(f);

    send_header(sock, "200 OK", ct, (int)len);

    char buf[4096];
    int done;

    while ((done = fread(buf, 1, sizeof(buf), f)) > 0) {
        write(sock, buf, done);
    }

    fclose(f);
}

/* ── API: EVENTS ─────────────────────────────── */

static void serve_events_json(int sock) {

    size_t bufsize = MAX_EVENTS * 512 + 32;
    char *buf = malloc(bufsize);
    if (!buf) {
        send_string(sock, "500 Internal Server Error",
                    "text/plain", "OOM");
        return;
    }

    size_t offset = 0;
    offset += snprintf(buf + offset, bufsize - offset, "[");

    pthread_mutex_lock(&repo_mutex);

    int first = 1;
    int start = event_count > 50 ? event_count - 50 : 0;

    for (int i = event_count - 1; i >= start; i--) {

        const char *type_s;
        switch (events[i].type) {
            case EVT_LOGIN_OK:   type_s = "LOGIN_OK"; break;
            case EVT_LOGIN_FAIL: type_s = "LOGIN_FAIL"; break;
            case EVT_LOGOUT:     type_s = "LOGOUT"; break;
            case EVT_DISCONNECT: type_s = "DISCONNECT"; break;
            default:             type_s = "UNKNOWN"; break;
        }

        if (!first) {
            offset += snprintf(buf + offset, bufsize - offset, ",");
        }

        offset += snprintf(buf + offset, bufsize - offset,
            "{\"type\":\"%s\",\"timestamp\":\"%s\","
            "\"username\":\"%s\",\"ip\":\"%s\","
            "\"port\":%d,\"service\":\"%s\"}",
            type_s,
            events[i].timestamp,
            events[i].username,
            events[i].client_ip,
            events[i].client_port,
            events[i].service
        );

        first = 0;
    }

    pthread_mutex_unlock(&repo_mutex);

    snprintf(buf + offset, bufsize - offset, "]");

    send_string(sock, "200 OK", "application/json", buf);

    free(buf);
}

/* ── API: ACTIVE ─────────────────────────────── */

static void serve_active_json(int sock) {

    size_t bufsize = MAX_ACTIVE * 512 + 32;
    char *buf = malloc(bufsize);
    if (!buf) {
        send_string(sock, "500 Internal Server Error",
                    "text/plain", "OOM");
        return;
    }

    size_t offset = 0;
    offset += snprintf(buf + offset, bufsize - offset, "[");

    pthread_mutex_lock(&repo_mutex);

    int first = 1;

    for (int i = 0; i < active_count; i++) {

        if (!active_sessions[i].active) continue;

        if (!first) {
            offset += snprintf(buf + offset, bufsize - offset, ",");
        }

        offset += snprintf(buf + offset, bufsize - offset,
            "{\"username\":\"%s\",\"ip\":\"%s\","
            "\"port\":%d,\"service\":\"%s\","
            "\"login_time\":\"%s\"}",
            active_sessions[i].username,
            active_sessions[i].client_ip,
            active_sessions[i].client_port,
            active_sessions[i].service,
            active_sessions[i].login_time
        );

        first = 0;
    }

    pthread_mutex_unlock(&repo_mutex);

    snprintf(buf + offset, bufsize - offset, "]");

    send_string(sock, "200 OK", "application/json", buf);

    free(buf);
}

/* ── REQUEST HANDLER ─────────────────────────────── */

static void process_request(int conn_sock) {

    char request_line[512];
    char header_line[512];

    read_line_crlf(conn_sock, request_line, sizeof(request_line));

    do {
        read_line_crlf(conn_sock, header_line, sizeof(header_line));
    } while (strlen(header_line) > 0);

    if (strncmp(request_line, "GET /", 5) != 0) {
        send_string(conn_sock, "405 Method Not Allowed",
                    "text/plain", "Method not supported");
        return;
    }

    char uri[256];
    snprintf(uri, sizeof(uri), "%s", request_line + 4);

    char *space = strchr(uri, ' ');
    if (space) *space = '\0';

    if (strstr(uri, "..")) {
        send_string(conn_sock, "403 Forbidden",
                    "text/plain", "Forbidden");
        return;
    }

    if (strcmp(uri, "/") == 0)
        strcpy(uri, "/index.html");

    if (strcmp(uri, "/api/events") == 0) {
        serve_events_json(conn_sock);
        return;
    }

    if (strcmp(uri, "/api/active") == 0) {
        serve_active_json(conn_sock);
        return;
    }

    char filepath[512];
    snprintf(filepath, sizeof(filepath), "%s%s", BASE_FOLDER, uri);

    send_file(conn_sock, filepath);
}

/* ── SERVER LOOP ─────────────────────────────── */

void http_server_run(void) {

    struct addrinfo hints, *res;
    int sock, conn_sock;
    char port_str[16];

    signal(SIGCHLD, SIG_IGN);

    memset(&hints, 0, sizeof(hints));
    hints.ai_family   = AF_INET6;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags    = AI_PASSIVE;

    snprintf(port_str, sizeof(port_str), "%d", HTTP_PORT);

    if (getaddrinfo(NULL, port_str, &hints, &res) != 0) {
        perror("getaddrinfo HTTP");
        return;
    }

    sock = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if (sock < 0) return;

    int yes = 1;
    setsockopt(sock, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes));

    int no = 0;
    setsockopt(sock, IPPROTO_IPV6, IPV6_V6ONLY, &no, sizeof(no));

    if (bind(sock, res->ai_addr, res->ai_addrlen) < 0) {
        perror("bind HTTP");
        close(sock);
        return;
    }

    freeaddrinfo(res);
    listen(sock, SOMAXCONN);

    printf("[HTTP] A escutar na porta %d\n", HTTP_PORT);

    while (1) {
        struct sockaddr_storage from;
        socklen_t from_len = sizeof(from);

        conn_sock = accept(sock, (struct sockaddr *)&from, &from_len);
        if (conn_sock < 0) continue;

        if (!fork()) {
            close(sock);
            process_request(conn_sock);
            close(conn_sock);
            exit(0);
        }

        close(conn_sock);
    }

    close(sock);
}