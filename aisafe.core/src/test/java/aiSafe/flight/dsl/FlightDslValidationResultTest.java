package aiSafe.flight.dsl;

import aiSafe.flight.domain.FlightType;
import aiSafe.flight.dsl.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightDslValidationResultTest {

    @Test
    void success_and_failure_factories() {
        final DslFlightPlan model = new DslFlightPlan(
                "TP1", FlightType.REGULAR,
                List.of(new DslLeg(
                        new DslSchedulePoint("OPO", "2026-01-01", "08:00", 1),
                        new DslSchedulePoint("LIS", "2026-01-01", "09:00", 2),
                        new DslRoute("OPO", "LIS", 3),
                        List.of(new DslSegment(
                                new DslCoordinates(1, 2),
                                new DslCoordinates(3, 4),
                                1000,
                                4)),
                        new DslFuel(100, "kg", 5),
                        1)),
                1);

        final FlightDslValidationResult ok = FlightDslValidationResult.success(model);
        assertTrue(ok.isValid());
        assertSame(model, ok.model().orElseThrow());
        assertTrue(ok.errors().isEmpty());
        assertTrue(ok.formattedErrors().isEmpty());

        final FlightDslError error = new FlightDslError(FlightDslErrorType.SYNTAX, "bad", 1, 1);
        final FlightDslValidationResult fail = FlightDslValidationResult.failure(List.of(error));
        assertFalse(fail.isValid());
        assertTrue(fail.model().isEmpty());
        assertEquals(1, fail.errors().size());
        assertEquals(error.format(), fail.formattedErrors().getFirst());
    }
}
