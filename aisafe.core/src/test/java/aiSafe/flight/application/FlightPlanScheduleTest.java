package aiSafe.flight.application;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanScheduleTest {

    @Test
    void overlapping_schedules_detect_conflict() {
        final var a = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 10, 0),
                LocalDateTime.of(2026, 6, 10, 11, 0));
        final var b = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 10, 30),
                LocalDateTime.of(2026, 6, 10, 11, 30));

        assertTrue(a.overlaps(b));
    }

    @Test
    void touching_at_boundary_does_not_overlap() {
        final var a = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 10, 0),
                LocalDateTime.of(2026, 6, 10, 11, 0));
        final var b = new FlightPlanSchedule(
                LocalDateTime.of(2026, 6, 10, 11, 0),
                LocalDateTime.of(2026, 6, 10, 12, 0));

        assertFalse(a.overlaps(b));
    }

    @Test
    void arrival_must_be_after_departure() {
        final LocalDateTime when = LocalDateTime.of(2026, 6, 10, 11, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new FlightPlanSchedule(when, when));
    }
}
