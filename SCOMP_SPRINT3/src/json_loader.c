#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <cjson/cJSON.h>

#include "common.h"
#include "controller.h"


int load_flight_plans(const char *filename, SimulationConfig *config, FlightPlan plans[])
{
    if (!filename || !config || !plans) {
        fprintf(stderr, "[ERROR] Invalid arguments\n");
        return 0;
    }

    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("[ERROR] fopen");
        return 0;
    }

    fseek(file, 0, SEEK_END);
    long size = ftell(file);
    rewind(file);

    char *json_data = malloc(size + 1);
    if (!json_data) {
        fclose(file);
        return 0;
    }

    fread(json_data, 1, size, file);
    json_data[size] = '\0';

    fclose(file);

    cJSON *root = cJSON_Parse(json_data);

    if (!root) {
        fprintf(stderr, "[ERROR] Invalid JSON\n");
        free(json_data);
        return 0;
    }

    // =========================================================
    // GET FLIGHTS ARRAY
    // =========================================================

    cJSON *flights = cJSON_GetObjectItem(root, "Flights");

    // Compatibilidade:
    // se não existir "Flights", assume JSON antigo com 1 voo
    int singleFlightMode = 0;

    if (!flights) {

        singleFlightMode = 1;

        flights = cJSON_CreateArray();

        // adiciona root como elemento do array
        cJSON_AddItemReferenceToArray(flights, root);
    }

    if (!cJSON_IsArray(flights)) {
        fprintf(stderr, "[ERROR] Flights is not an array\n");

        if (singleFlightMode == 0)
            cJSON_Delete(root);

        free(json_data);
        return 0;
    }

    int numFlights = cJSON_GetArraySize(flights);

    if (numFlights <= 0) {
        fprintf(stderr, "[ERROR] No flights found\n");

        if (singleFlightMode == 0)
            cJSON_Delete(root);

        free(json_data);
        return 0;
    }

    config->numFlights = numFlights;

    // =========================================================
    // LOOP FLIGHTS
    // =========================================================

    for (int f = 0; f < numFlights; f++) {

        cJSON *flight = cJSON_GetArrayItem(flights, f);

        if (!cJSON_IsObject(flight)) {
            fprintf(stderr, "[ERROR] Flight %d invalid\n", f);
            continue;
        }

        FlightPlan *fp = &plans[f];

        memset(fp, 0, sizeof(*fp));

        // =====================================================
        // BASIC INFO
        // =====================================================

        cJSON *id = cJSON_GetObjectItem(flight, "ID");

        if (!cJSON_IsNumber(id)) {
            fprintf(stderr, "[ERROR] Flight %d missing ID\n", f);
            continue;
        }

        fp->id = id->valueint;

        config->flightIds[f] = fp->id;

        cJSON *route = cJSON_GetObjectItem(flight, "Route");

        if (cJSON_IsString(route) && route->valuestring) {
            strncpy(fp->route,
                    route->valuestring,
                    sizeof(fp->route) - 1);
        }

        cJSON *departureDate = cJSON_GetObjectItem(flight, "DepartureDate");

        if (cJSON_IsString(departureDate) && departureDate->valuestring) {
            strncpy(fp->departureDate,
                    departureDate->valuestring,
                    sizeof(fp->departureDate) - 1);
        } else {
            fprintf(stderr, "[ERROR] Flight %d missing departure date\n", f);
            continue;
        }

        cJSON *departureTime = cJSON_GetObjectItem(flight, "DepartureTime");

        if (cJSON_IsString(departureTime) && departureTime->valuestring) {
            strncpy(fp->departureTime,
                    departureTime->valuestring,
                    sizeof(fp->departureTime) - 1);
        } else {
            fprintf(stderr, "[ERROR] Flight %d missing departure time\n", f);
            continue;
        }

        // =====================================================
        // LEG
        // =====================================================

        cJSON *legs = cJSON_GetObjectItem(flight, "Leg");

        if (!cJSON_IsArray(legs) ||
            cJSON_GetArraySize(legs) == 0) {

            fprintf(stderr,
                    "[ERROR] Flight %d missing Leg\n",
                    f);

            continue;
        }

        cJSON *leg = cJSON_GetArrayItem(legs, 0);

        if (!cJSON_IsObject(leg)) {
            fprintf(stderr,
                    "[ERROR] Flight %d invalid Leg\n",
                    f);

            continue;
        }

        // Departure / Arrival

        cJSON *departure =
            cJSON_GetObjectItem(leg, "Departure");

        cJSON *arrival =
            cJSON_GetObjectItem(leg, "Arrival");

        if (cJSON_IsString(departure) &&
            departure->valuestring) {

            strncpy(fp->departure,
                    departure->valuestring,
                    sizeof(fp->departure) - 1);
        }

        if (cJSON_IsString(arrival) &&
            arrival->valuestring) {

            strncpy(fp->arrival,
                    arrival->valuestring,
                    sizeof(fp->arrival) - 1);
        }

        // Fuel Quantity

        cJSON *fuel = cJSON_GetObjectItem(leg, "Fuel");
        if (cJSON_IsObject(fuel)) {
            cJSON *qty = cJSON_GetObjectItem(fuel, "Quantity");
            if (cJSON_IsNumber(qty))
                fp->fuelQuantity = qty->valuedouble;
        }

        // =====================================================
        // SEGMENTS
        // =====================================================

        cJSON *segments =
            cJSON_GetObjectItem(leg, "Segments");

        if (!cJSON_IsArray(segments)) {
            fprintf(stderr,
                    "[ERROR] Flight %d missing Segments\n",
                    f);

            continue;
        }

        int numSegments = cJSON_GetArraySize(segments);

        if (numSegments > 20)
            numSegments = 20;

        fp->numSegments = numSegments;

        for (int i = 0; i < numSegments; i++) {

            cJSON *segment =
                cJSON_GetArrayItem(segments, i);

            if (!cJSON_IsObject(segment))
                continue;

            // =================================================
            // MODE
            // =================================================

            cJSON *mode =
                cJSON_GetObjectItem(segment, "Mode");

            if (cJSON_IsString(mode) &&
                mode->valuestring) {

                strncpy(fp->segments[i].mode,
                        mode->valuestring,
                        sizeof(fp->segments[i].mode) - 1);
            }

            // =================================================
            // START
            // =================================================

            cJSON *start =
                cJSON_GetObjectItem(segment, "Start");

            if (cJSON_IsObject(start)) {

                cJSON *lat =
                    cJSON_GetObjectItem(start, "Latitude");

                cJSON *lon =
                    cJSON_GetObjectItem(start, "Longitude");

                if (cJSON_IsNumber(lat))
                    fp->segments[i].start.latitude =
                        lat->valuedouble;

                if (cJSON_IsNumber(lon))
                    fp->segments[i].start.longitude =
                        lon->valuedouble;

                cJSON *alt =
                    cJSON_GetObjectItem(start, "Altitude");

                if (cJSON_IsObject(alt)) {

                    cJSON *q =
                        cJSON_GetObjectItem(alt, "Quantity");

                    cJSON *u =
                        cJSON_GetObjectItem(alt, "Unit");

                    if (cJSON_IsNumber(q))
                        fp->segments[i]
                            .start.altitude.value =
                            q->valuedouble;

                    if (cJSON_IsString(u) &&
                        u->valuestring) {

                        strncpy(
                            fp->segments[i]
                                .start.altitude.unit,
                            u->valuestring,
                            sizeof(fp->segments[i]
                                       .start.altitude.unit) - 1);
                    }
                }
            }

            // =================================================
            // END
            // =================================================

            cJSON *end =
                cJSON_GetObjectItem(segment, "End");

            if (cJSON_IsObject(end)) {

                cJSON *lat =
                    cJSON_GetObjectItem(end, "Latitude");

                cJSON *lon =
                    cJSON_GetObjectItem(end, "Longitude");

                if (cJSON_IsNumber(lat))
                    fp->segments[i].end.latitude =
                        lat->valuedouble;

                if (cJSON_IsNumber(lon))
                    fp->segments[i].end.longitude =
                        lon->valuedouble;

                cJSON *alt =
                    cJSON_GetObjectItem(end, "Altitude");

                if (cJSON_IsObject(alt)) {

                    cJSON *q =
                        cJSON_GetObjectItem(alt, "Quantity");

                    cJSON *u =
                        cJSON_GetObjectItem(alt, "Unit");

                    if (cJSON_IsNumber(q))
                        fp->segments[i]
                            .end.altitude.value =
                            q->valuedouble;

                    if (cJSON_IsString(u) &&
                        u->valuestring) {

                        strncpy(
                            fp->segments[i]
                                .end.altitude.unit,
                            u->valuestring,
                            sizeof(fp->segments[i]
                                       .end.altitude.unit) - 1);
                    }
                }
            }
        }
    }

    // cleanup

    if (singleFlightMode == 0)
        cJSON_Delete(root);
    else
        cJSON_Delete(flights);

    free(json_data);

    return 1;
}
