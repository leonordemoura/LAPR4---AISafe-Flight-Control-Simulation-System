#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <limits.h>

#include "common.h"

#define EARTH_RADIUS_KM 6371.0

int convert_into_timestamp(const char *time){
    if (!time)
            return -1;

    int hours = 0, minutes = 0;

    if (sscanf(time, "%d:%d", &hours, &minutes) != 2) {
        fprintf(stderr, "[ERROR] Invalid time format: \"%s\" (expected HH:MM)\n", time);
        return -1;
    }

    if (hours < 0 || hours > 23 || minutes < 0 || minutes > 59) {
        fprintf(stderr, "[ERROR] Time out of range: \"%s\"\n", time);
        return -1;
    }

    return hours * 60 + minutes;
}

void turn_time_into_char(int timestamp, char *buf, size_t buf_size){
    if (!buf || buf_size < 6)
        return;

    int hours   = (timestamp / 60) % 24;
    int minutes = timestamp % 60;

    snprintf(buf, buf_size, "%02d:%02d", hours, minutes);
}

int find_closest_position(FlightHistory *hist, long target_ts){
    int closest = -1;
    long min_diff = LONG_MAX;

    for (int j = 0; j < hist->count; j++) {
        long diff = labs(hist->positions[j].timestamp - target_ts);
        if (diff < min_diff) {
            min_diff = diff;
            closest = j;
        }
    }

    return closest;
}

double calculate_kilometers(double x1, double y1, double x2, double y2) {
    // Converter graus para radianos
    double lat1_rad = x1 * M_PI / 180.0;
    double lon1_rad = y1 * M_PI / 180.0;
    double lat2_rad = x2 * M_PI / 180.0;
    double lon2_rad = y2 * M_PI / 180.0;

    // Calcular diferenças
    double dlat = lat2_rad - lat1_rad;
    double dlon = lon2_rad - lon1_rad;

    // Fórmula de Haversine (Padrão para calcular distância entre dois pontos na superfície da Terra)
    double a = sin(dlat/2) * sin(dlat/2) + cos(lat1_rad) * cos(lat2_rad) *sin(dlon/2) * sin(dlon/2);
    double c = 2 * atan2(sqrt(a), sqrt(1-a));

    return EARTH_RADIUS_KM * c;
}

void compute_velocity_vector(FlightPosition *pos1, FlightPosition *pos2, VelocityVector *vv) {
    if (!pos1 || !pos2 || !vv) return;

    double dt = (double)(pos2->timestamp - pos1->timestamp); // segundos
    if (fabs(dt) < 1e-9) {
        dt = 60.0;
    }

    // converte deslocamento em graus para km usando Haversine
    double dx_km = calculate_kilometers(pos1->x, pos1->y, pos2->x, pos1->y); // componente lat
    double dy_km = calculate_kilometers(pos1->x, pos1->y, pos1->x, pos2->y); // componente lon
    double dz_m  = pos2->altitude - pos1->altitude;

    // sinal: pode estar a ir para sul/oeste
    if (pos2->x < pos1->x) dx_km = -dx_km;
    if (pos2->y < pos1->y) dy_km = -dy_km;

    double dt_h = dt / 3600.0; // converter segundos para horas

    vv->vx = dx_km / dt_h; // km/h
    vv->vy = dy_km / dt_h; // km/h
    vv->vz = dz_m  / dt; // m/s
}