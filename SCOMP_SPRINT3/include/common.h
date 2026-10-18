#ifndef COMMON_H
#define COMMON_H

#include <time.h>
#include <pthread.h>
#include <semaphore.h>

#define MAX_FLIGHTS 70
#define MAX_HISTORY 1000

#define MAX_VIOLATIONS 50
#define MAX_TOTAL_VIOLATIONS 500

#define SHM_NAME  "/simulation_shm"
#define SEM_MUTEX "/sim_mutex"
#define SEM_STEP  "/sim_step"
#define SEM_DONE  "/atc_done"

// ---------------- POSITION RECEIVED FROM FLIGHT ----------------
typedef struct {
    int flightId;
    double x;
    double y;
    double altitude;
    double vx;
    double vy;
    double vz;
} FlightPositionShared;

// ---------------- CONFIGURATION ----------------
typedef enum {
    WEATHER_CLEAR,
    WEATHER_RAIN,
    WEATHER_STORM
} WeatherCondition;

typedef struct {

    // tempo
    int startTime;
    int endTime;

    // área
    double minX;
    double maxX;
    double minY;
    double maxY;

    // voos
    int numFlights;
    int flightIds[MAX_FLIGHTS];

    // meteorologia
    WeatherCondition weatherCondition;

    // performance
    double simulationSpeed;
    double updateInterval;

} SimulationConfig;

typedef struct {
    double value;
    char unit[20];
} Measurement;

typedef struct {
    Measurement altitude;
    Measurement speed;
    double rateClimb;
} ClimbProfile;

typedef struct {
    int flightId;
    FlightPositionShared positions[MAX_HISTORY];
    int count;
    int violation_count;
} FlightHistory;

typedef struct {
    double latitude;
    double longitude;
    Measurement altitude;
} Waypoint;

typedef struct {
    char mode[20];      // "climb", "cruise", "descend"
    Waypoint start;
    Waypoint end;
    Measurement speed;  // nós (Knots)
    double rate;        // m/s (climb ou descent; 0 em cruise)
} FlightSegment;

typedef struct {

    int id;
    char departureDate[11];
    char departureTime[6];

    char route[20];

    char departure[10];
    char arrival[10];

    FlightSegment segments[20];
    int numSegments;

    double fuelQuantity;
} FlightPlan;

// ---------------- SAFETY VIOLATION DETECTION ----------------

typedef struct {
    double horizontal_separation;
    double vertical_separation;
    int violation_threshold;
} SafetyRules;

typedef struct {
    long timestamp_sec;
    int global_id;

    int flightA;
    FlightPositionShared posA;

    int flightB;
    FlightPositionShared posB;

    double horizontal_distance;
    double vertical_distance;
} SafetyViolationEvent;


typedef struct {
    // --- Dados dos Voos (Escritos pelos Voos, Lidos pelo Simulador) ---
    FlightPositionShared positions[MAX_FLIGHTS];
    int active[MAX_FLIGHTS];

    // US109: Histórico de trajetória para o relatório final
    FlightHistory histories[MAX_FLIGHTS];

    // --- Controle da Simulação (Lido/Escrito pelo Simulador) ---
    int simulation_running;
    int current_step;

    // Regras de segurança
    SafetyRules rules;

    // US107: Matriz para evitar contagens repetidas da mesma violação
    int violation_active[MAX_FLIGHTS][MAX_FLIGHTS];

    // --- Comunicação US107 (Entre threads do Simulador) ---
    // Estes campos são usados para o padrão Produtor-Consumidor (Violation -> Report)
    int new_violation_flag;
    int num_violations;
    int global_violation_count;
    SafetyViolationEvent violations[MAX_VIOLATIONS];

    // US109: Histórico total de violações para o relatório final
    SafetyViolationEvent all_violations[MAX_TOTAL_VIOLATIONS];

} SimulationSharedState;

#endif