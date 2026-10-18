package aiSafe.flight.application;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFlightPlanLegInputTest {

    @Test
    void rejects_null_segments() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegisterFlightPlanLegInput(
                        "OPO", "2026-01-01", "08:00",
                        "LIS", "2026-01-01", "09:00",
                        "OPO", "LIS",
                        1000, "kg",
                        null
                ));
    }

    @Test
    void rejects_empty_segments() {
        assertThrows(IllegalArgumentException.class,
                () -> new RegisterFlightPlanLegInput(
                        "OPO", "2026-01-01", "08:00",
                        "LIS", "2026-01-01", "09:00",
                        "OPO", "LIS",
                        1000, "kg",
                        Collections.emptyList()
                ));
    }

    @Test
    void segments_list_is_unmodifiable() {
        final RegisterFlightPlanLegInput leg = RegisterFlightPlanTestFixtures.sampleLeg();
        assertThrows(UnsupportedOperationException.class,
                () -> leg.segments().add(RegisterFlightPlanTestFixtures.sampleSegment()));
    }

    @Test
    void accepts_multiple_segments() {
        final RegisterFlightPlanLegInput leg = new RegisterFlightPlanLegInput(
                "OPO", "2026-01-01", "08:00",
                "LIS", "2026-01-01", "09:00",
                "OPO", "LIS",
                1000, "kg",
                List.of(
                        RegisterFlightPlanTestFixtures.sampleSegment(),
                        new RegisterFlightPlanSegmentInput(40.0, -8.0, 39.0, -8.5, 11000, 10, 90, 10, "kt")
                )
        );
        assertEquals(2, leg.segments().size());
    }
}
