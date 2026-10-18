#ifndef COMMON_H
#define COMMON_H

#include <time.h>

#define MAX_FLIGHTS 70
#define MAX_HISTORY 1000

// ---------------- POSITION RECEIVED FROM FLIGHT ----------------
typedef struct {
    int flightId;
    double x;
    double y;
    double altitude;
    long timestamp;
} FlightPosition;

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

// ---------------- SIMULATION STATE (for history) ----------------
typedef struct {
    FlightPosition positions[MAX_HISTORY];
    int count;
    int violation_count;
} FlightHistory;

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
    double vx;
    double vy;
    double vz;
} VelocityVector;

typedef struct {
    long timestamp_sec;

    int flightA;
    FlightPosition posA;
    VelocityVector velA;

    int flightB;
    FlightPosition posB;
    VelocityVector velB;

    double horizontal_distance;
    double vertical_distance;
} SafetyViolationEvent;

#endif