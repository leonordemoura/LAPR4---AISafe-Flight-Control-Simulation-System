package aiSafe.flight.application.importplan;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportFlightPlanTypesTest {

    @Test
    void importFlightPlanError_format_with_and_without_line_column() {
        final ImportFlightPlanError simple = new ImportFlightPlanError(
                ImportFlightPlanErrorCode.FLIGHT_NOT_FOUND, "missing");
        assertEquals("FLIGHT_NOT_FOUND: missing", simple.format());

        final ImportFlightPlanError detailed = new ImportFlightPlanError(
                ImportFlightPlanErrorCode.DSL_VALIDATION, "bad dsl", 4, 12);
        assertEquals("DSL_VALIDATION [Line 4, Column 12]: bad dsl", detailed.format());
        assertEquals(4, detailed.line());
        assertEquals(12, detailed.column());
        assertEquals(ImportFlightPlanErrorCode.DSL_VALIDATION, detailed.code());
        assertNull(simple.line());
        assertNull(simple.column());
        assertEquals("missing", simple.message());
    }

    @Test
    void importFlightPlanResult_success_and_failure() {
        final Flight flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
        final FlightPlan plan = FlightPlan.fromValidatedImport(sampleDslPlan());

        final ImportFlightPlanResult ok =
                ImportFlightPlanResult.success(flight, plan);
        assertTrue(ok.isSuccess());
        assertSame(flight, ok.flight().orElseThrow());
        assertSame(plan, ok.flightPlan().orElseThrow());
        assertTrue(ok.errors().isEmpty());
        assertTrue(ok.formattedErrors().isEmpty());

        final ImportFlightPlanResult fail = ImportFlightPlanResult.failure(List.of(
                new ImportFlightPlanError(ImportFlightPlanErrorCode.EMPTY_FILE, "empty")));
        assertFalse(fail.isSuccess());
        assertTrue(fail.flight().isEmpty());
        assertTrue(fail.flightPlan().isEmpty());
        assertEquals(1, fail.formattedErrors().size());
        assertThrows(UnsupportedOperationException.class, () -> fail.errors().add(
                new ImportFlightPlanError(ImportFlightPlanErrorCode.EMPTY_FILE, "x")));
    }

    private static aiSafe.flight.dsl.model.DslFlightPlan sampleDslPlan() {
        final var leg = new aiSafe.flight.dsl.model.DslLeg(
                new aiSafe.flight.dsl.model.DslSchedulePoint("OPO", "2026-05-11", "08:00", 1),
                new aiSafe.flight.dsl.model.DslSchedulePoint("LIS", "2026-05-11", "09:00", 2),
                new aiSafe.flight.dsl.model.DslRoute("OPO", "LIS", 3),
                List.of(new aiSafe.flight.dsl.model.DslSegment(
                        new aiSafe.flight.dsl.model.DslCoordinates(41.0, -8.0),
                        new aiSafe.flight.dsl.model.DslCoordinates(38.0, -9.0),
                        10000,
                        4)),
                new aiSafe.flight.dsl.model.DslFuel(5000, "kg", 5),
                1);
        return new aiSafe.flight.dsl.model.DslFlightPlan(
                "TP123", FlightType.REGULAR, List.of(leg), 1);
    }
}
