#ifndef FLIGHT_H
#define FLIGHT_H

void run_flight(const FlightPlan *plan, int write_fd, int ready_fd, int updateInterval);

#endif