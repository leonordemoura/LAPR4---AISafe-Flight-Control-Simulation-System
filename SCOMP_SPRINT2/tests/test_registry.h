#ifndef TEST_REGISTRY_H
#define TEST_REGISTRY_H

void test_convert_valid();
void test_convert_invalid();
void test_turn_time_into_char();
void test_find_closest_position();
void test_velocity_normal();
void test_velocity_zero_dt();
void test_velocity_null();


void test_flight();

void test_valid_config();
void test_invalid_time();
void test_invalid_numFlights();
void test_invalid_area();
void test_invalid_weather();
void test_invalid_performance();

void test_simulation_speed_too_high();
void test_update_interval_too_large();
void test_geographic_bounds_out_of_range();

void test_valid_rules();
void test_invalid_horizontal_separation();
void test_invalid_vertical_separation();
void test_invalid_threshold();

void test_valid_plan();
void test_invalid_flight_id();
void test_no_segments();
void test_invalid_segment_mode();

void test_valid_area_bounds();
void test_invalid_area_bounds();

void test_violation_positive();
void test_violation_horizontal_safe();
void test_violation_altitude_safe();
void test_violation_time_safe();
void test_violation_edge();
void test_violation_time_overlay_fail();
void test_violation_time_overlay_success();
void test_fuel_sufficient();
void test_fuel_insufficient();
void test_fuel_exact_limit();


void test_invalid_config_blocks();
void test_log_creation();
void test_smoke_simulation();

void test_validation();

void test_flight_sigterm();
void test_flight_sigusr1();
void test_flight_sigusr2_step();
void test_multiple_steps();


void test_flight_null_plan();
void test_flight_pipe_output();
void test_flight_timestamp_order();
void test_flight_sigterm_stop();

void test_single_step();
void test_no_autorization_no_move();
void test_step_sequence();
void test_no_deadlock();
void test_multiple_sigusr2_safe();

#endif