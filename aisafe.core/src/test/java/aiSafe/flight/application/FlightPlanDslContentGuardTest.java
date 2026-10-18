package aiSafe.flight.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanDslContentGuardTest {

    @Test
    void accepts_complete_dsl() {
        assertDoesNotThrow(() -> FlightPlanDslContentGuard.ensureMinimumStructure("""
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-12-15 10:00
                        arrival LIS 2026-12-15 11:00
                    }
                }
                """));
    }

    @Test
    void rejects_header_only() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> FlightPlanDslContentGuard.ensureMinimumStructure("flight TP123 {"));
        assertTrue(ex.getMessage().contains("type"));
    }
}
