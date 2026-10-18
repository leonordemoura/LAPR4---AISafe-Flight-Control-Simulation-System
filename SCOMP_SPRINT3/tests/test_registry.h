#ifndef TEST_REGISTRY_H
#define TEST_REGISTRY_H

//validation
void test_config_valid_basic();
void test_config_invalid_speed_low();
void test_config_invalid_speed_high();
void test_config_invalid_time();
void test_config_invalid_flights();
void test_config_invalid_area();
void test_rules_valid();
void test_rules_invalid();
void test_plan_valid();
void test_plan_invalid_id();
void test_plan_invalid_segments();
void test_plan_invalid_mode();
void test_area_valid();
void test_area_invalid_start();
void test_area_invalid_end();

//us105
void test_us105_multi_flight_start();
void test_us105_invalid_config_blocks_start();
void test_us105_invalid_rules_blocks_start();
void test_us105_shared_memory_init();
void test_us105_early_stop();

//us106
void test_us106_threads_start();
void test_us106_violation_stress();
void test_us106_no_violation();

//us107
void test_us107_condition_signal();
void test_us107_log_written_on_violation();
void test_us107_report_not_blocking();
void test_us107_no_false_wakeup();

//us108
void test_us108_lockstep();
void test_us108_lockstep_order();
void test_us108_multi_flight_sync();
void test_us108_strict_lockstep();

//us109
void test_us109_report_generated();
void test_us109_report_content_basic();
void test_us109_report_contains_violations();

#endif