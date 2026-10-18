#include <stdio.h>

// headers dos testes
#include "test_registry.h"

int main() {

    printf("========== RUNNING TESTS ==========\n\n");

    printf("========== VALIDATION TESTS ==========\n\n");

    test_config_valid_basic();

        test_config_invalid_speed_low();
        test_config_invalid_speed_high();
        test_config_invalid_time();
        test_config_invalid_flights();
        test_config_invalid_area();

        test_rules_valid();
        test_rules_invalid();

        test_plan_valid();
        test_plan_invalid_id();
        test_plan_invalid_segments();
        test_plan_invalid_mode();

        test_area_valid();
        test_area_invalid_start();
        test_area_invalid_end();

    printf("\nALL VALIDATION TESTS PASSED\n");

    printf("\n===== US105 TESTS =====\n");

    test_us105_multi_flight_start();
    test_us105_invalid_config_blocks_start();
    test_us105_invalid_rules_blocks_start();
    test_us105_shared_memory_init();
    test_us105_early_stop();

    printf("\nALL US105 TESTS PASSED\n");

    printf("\n===== US106 TESTS =====\n");

    test_us106_threads_start();
    test_us106_violation_stress();
    test_us106_no_violation();

    printf("\nALL US106 TESTS PASSED\n");

    printf("\n===== US107 TESTS =====\n");

    test_us107_condition_signal();
    test_us107_log_written_on_violation();
    test_us107_report_not_blocking();
    test_us107_no_false_wakeup();

    printf("\nALL US107 TESTS PASSED\n");

    printf("\n===== US108 TESTS =====\n");

    test_us108_lockstep();
    test_us108_lockstep_order();
    test_us108_multi_flight_sync();
    test_us108_strict_lockstep();

    printf("\nALL US108 TESTS PASSED\n");

    printf("\n===== US109 TESTS =====\n");
    test_us109_report_generated();
    test_us109_report_content_basic();
    test_us109_report_contains_violations();
    printf("\nALL US109 TESTS PASSED\n");

    printf("\n========== ALL TESTS PASSED ==========\n");

    return 0;
}