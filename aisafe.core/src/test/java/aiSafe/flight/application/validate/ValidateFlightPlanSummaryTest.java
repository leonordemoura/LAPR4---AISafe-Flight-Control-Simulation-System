package aiSafe.flight.application.validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidateFlightPlanSummaryTest {

    @Test
    void formattedLines_include_key_fields() {
        final ValidateFlightPlanSummary summary = new ValidateFlightPlanSummary(
                "TP123",
                "OPO-LIS",
                "CS-TUA",
                "A320",
                "P001",
                "2026-07-07T10:00",
                "2026-07-07T11:00",
                1,
                1,
                9000,
                3200,
                2100,
                2,
                0.8,
                100,
                69.0,
                69.0);

        final String text = String.join("\n", summary.formattedLines());

        assertTrue(text.contains("TP123"));
        assertTrue(text.contains("A320"));
        assertTrue(text.contains("3200"));
        assertTrue(text.contains("1900")); // 2100 - 200 taxi
        assertTrue(text.contains("2200")); // 2100 + 100 reserve
        assertTrue(text.contains("ISA"));
        assertTrue(text.contains("Mach 0.82"));
    }
}
