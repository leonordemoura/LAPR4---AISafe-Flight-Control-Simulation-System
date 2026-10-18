#include <assert.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <time.h>
#include <errno.h>
#include <math.h>

#include "../include/common.h"
#include "../include/controller.h"
#include "../include/flight.h"
#include "../include/util.h"

/* -----------------------------
   TEST 1: convert_into_timestamp (valid)
------------------------------*/
void test_convert_valid() {
    printf("\nTESTE1\n");
    assert(convert_into_timestamp("00:00") == 0);
    assert(convert_into_timestamp("01:00") == 60);
    assert(convert_into_timestamp("10:30") == 630);
    assert(convert_into_timestamp("23:59") == 23 * 60 + 59);

    printf("[OK] convert_into_timestamp valid cases\n");
}

/* -----------------------------
   TEST 2: convert_into_timestamp (invalid)
------------------------------*/
void test_convert_invalid() {
    printf("\nTESTE2\n");
    assert(convert_into_timestamp("abc") == -1);
    assert(convert_into_timestamp("25:00") == -1);
    assert(convert_into_timestamp("10:99") == -1);
    assert(convert_into_timestamp("10-30") == -1);

    printf("[OK] convert_into_timestamp invalid cases\n");
}

/* -----------------------------
   TEST 3: turn_time_into_char
------------------------------*/
void test_turn_time_into_char() {
    printf("\nTESTE3\n");

    char buf[10];

    turn_time_into_char(0, buf, sizeof(buf));
    assert(strcmp(buf, "00:00") == 0);

    turn_time_into_char(60, buf, sizeof(buf));
    assert(strcmp(buf, "01:00") == 0);

    turn_time_into_char(1439, buf, sizeof(buf)); // 23:59
    assert(strcmp(buf, "23:59") == 0);

    // buffer pequeno (não deve crashar)
    char small[3];
    turn_time_into_char(60, small, sizeof(small));

    printf("[OK] turn_time_into_char tests\n");
}

/* -----------------------------
   TEST 4: find_closest_position
------------------------------*/
void test_find_closest_position() {
    printf("\nTESTE4\n");

    FlightHistory hist;
    memset(&hist, 0, sizeof(hist));

    hist.count = 3;

    hist.positions[0].timestamp = 100;
    hist.positions[1].timestamp = 200;
    hist.positions[2].timestamp = 300;

    assert(find_closest_position(&hist, 190) == 1);
    assert(find_closest_position(&hist, 205) == 1);
    assert(find_closest_position(&hist, 50) == 0);
    assert(find_closest_position(&hist, 1000) == 2);

    printf("[OK] find_closest_position tests\n");
}

/* -----------------------------
   TEST 5: compute_velocity_vector (normal)
------------------------------*/
void test_velocity_normal() {
    printf("\nTESTE5\n");

    FlightPosition p1 = { .x = 0, .y = 0, .altitude = 0, .timestamp = 0 };
    FlightPosition p2 = { .x = 10, .y = 20, .altitude = 30, .timestamp = 10 };

    VelocityVector v;

    compute_velocity_vector(&p1, &p2, &v);

    // dt = 10 segundos → dt_h = 10/3600 horas
    double dt_h = 10.0 / 3600.0;

    // deslocamento real em km (Haversine)
    double dx_km = calculate_kilometers(0, 0, 10, 0);
    double dy_km = calculate_kilometers(0, 0, 0, 20);

    double expected_vx = dx_km / dt_h;   // km/h
    double expected_vy = dy_km / dt_h;   // km/h
    double expected_vz = 30.0 / 10.0;    // m/s

    assert(fabs(v.vx - expected_vx) < 1e-6);
    assert(fabs(v.vy - expected_vy) < 1e-6);
    assert(fabs(v.vz - expected_vz) < 1e-6);

    printf("[OK] compute_velocity_vector normal case\n");
}

/* -----------------------------
   TEST 6: compute_velocity_vector (dt = 0)
------------------------------*/
void test_velocity_zero_dt() {
    printf("\nTESTE6\n");

    FlightPosition p1 = { .x = 1, .y = 1, .altitude = 1, .timestamp = 100 };
    FlightPosition p2 = { .x = 2, .y = 2, .altitude = 2, .timestamp = 100 }; // dt = 0

    VelocityVector v;

    compute_velocity_vector(&p1, &p2, &v);

    // dt é forçado para 60 segundos
    double dt = 60.0;

    // deslocamentos reais
    double dx_km = calculate_kilometers(1, 1, 2, 1);
    double dy_km = calculate_kilometers(1, 1, 1, 2);
    double dz_m  = 2 - 1;

    double dt_h = dt / 3600.0;

    double expected_vx = dx_km / dt_h;  // km/h
    double expected_vy = dy_km / dt_h;  // km/h
    double expected_vz = dz_m  / dt;    // m/s

    assert(fabs(v.vx - expected_vx) < 1e-6);
    assert(fabs(v.vy - expected_vy) < 1e-6);
    assert(fabs(v.vz - expected_vz) < 1e-6);

    printf("[OK] compute_velocity_vector zero dt\n");
}

/* -----------------------------
   TEST 7: compute_velocity_vector (NULL safety)
------------------------------*/
void test_velocity_null() {
    printf("\nTESTE7\n");

    VelocityVector v = { .vx = 1, .vy = 1, .vz = 1 };

    compute_velocity_vector(NULL, NULL, &v);

    // não deve alterar valores
    assert(v.vx == 1 && v.vy == 1 && v.vz == 1);

    printf("[OK] compute_velocity_vector null safety\n");
}

/* -----------------------------
   TEST 8: calculate_kilometers
------------------------------*/
void test_calculate_kilometers() {
    printf("\nTESTE8\n");

    // Mesmo ponto → distância 0
    assert(fabs(calculate_kilometers(0, 0, 0, 0)) < 1e-6);

    // Lisboa → Porto (aprox. 275 km)
    double d = calculate_kilometers(38.7169, -9.1395, 41.1579, -8.6291);
    assert(d > 270.0 && d < 285.0);

    // Distância simétrica: A→B == B→A
    double d1 = calculate_kilometers(48.8566, 2.3522, 51.5074, -0.1278); // Paris→Londres
    double d2 = calculate_kilometers(51.5074, -0.1278, 48.8566, 2.3522); // Londres→Paris
    assert(fabs(d1 - d2) < 1e-6);

    // Pontos antipodais (máximo ~20015 km)
    double d3 = calculate_kilometers(0, 0, 0, 180);
    assert(d3 > 20000.0 && d3 < 20100.0);

    printf("[OK] calculate_kilometers tests\n");
}