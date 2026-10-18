#ifndef UTIL_H
#define UTIL_H

int find_closest_position(FlightHistory *hist, long target_ts);
int convert_into_timestamp(const char *time);
void turn_time_into_char(int timestamp, char *buf, size_t buf_size);
void compute_velocity_vector(FlightPosition *pos1, FlightPosition *pos2, VelocityVector *vv);
double calculate_kilometers(double x1, double y1, double x2, double y2);

#endif