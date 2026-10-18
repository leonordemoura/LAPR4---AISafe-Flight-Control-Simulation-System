#include <stdio.h>

// headers dos testes
#include "test_registry.h"

int main() {

    printf("========== RUNNING TESTS ==========\n\n");

    printf("========== VALIDATION TESTS ==========\n\n");
    test_valid_config();
    test_invalid_time();
    test_invalid_numFlights();
    test_invalid_area();
    test_invalid_weather();
    test_invalid_performance();

     test_simulation_speed_too_high();
    test_update_interval_too_large();
    test_geographic_bounds_out_of_range();

    test_valid_rules();
    test_invalid_horizontal_separation();
    test_invalid_vertical_separation();
    test_invalid_threshold();

    test_valid_plan();
    test_invalid_flight_id();
    test_no_segments();
    test_invalid_segment_mode();

    test_valid_area_bounds();
    test_invalid_area_bounds();

    printf("\nALL VALIDATION TESTS PASSED\n");

    printf("\n===== SIMULATION TESTS =====\n");

    test_invalid_config_blocks();
    test_log_creation();
    test_smoke_simulation();

    printf("\nALL SIMULATION TESTS PASSED\n");

    printf("\n===== SIGNALS TESTS =====\n");
    test_flight_sigterm();
    test_flight_sigusr1();
    test_flight_sigusr2_step();
    test_multiple_steps();
    printf("\nALL SIGNALS TESTS PASSED\n");

    printf("\n===== FLIGHT TESTS =====\n");
    test_flight_null_plan();
    test_flight_pipe_output();
    test_flight_timestamp_order();
    test_flight_sigterm_stop();
    printf("\nALL FLIGHT TESTS PASSED\n");

    printf("\n===== UTIL TESTS =====\n");
    test_convert_valid();
    test_convert_invalid();
    test_turn_time_into_char();
    test_find_closest_position();
    test_velocity_normal();
    test_velocity_zero_dt();
    test_velocity_null();
    printf("\nALL UTIL TESTS PASSED\n");

    printf("\n===== VIOLATION TESTS =====\n");
    test_violation_positive();
    test_violation_horizontal_safe();
    test_violation_altitude_safe();
    test_violation_time_safe();
    test_violation_edge();
    test_violation_time_overlay_fail();
    test_violation_time_overlay_success();
    test_fuel_sufficient();
    test_fuel_insufficient();
    test_fuel_exact_limit();
    printf("\nALL VIOLATION TESTS PASSED\n");

    printf("\n===== STEPS TESTS =====\n");
    test_single_step();
    test_no_autorization_no_move();
    test_step_sequence();
    test_no_deadlock();
    test_multiple_sigusr2_safe();
    printf("\nALL STEPS TESTS PASSED\n");

    printf("\n========== ALL TESTS PASSED ==========\n");

    return 0;
}