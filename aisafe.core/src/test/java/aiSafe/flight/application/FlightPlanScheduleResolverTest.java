package aiSafe.flight.application;

import aiSafe.flight.dsl.model.*;
import aiSafe.flight.domain.FlightType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanScheduleResolverTest {

    @Test
    void resolves_from_dsl_flight_plan() {
        final DslFlightPlan plan = samplePlan();
        final FlightPlanSchedule schedule = FlightPlanScheduleResolver.fromDslFlightPlan(plan);

        assertEquals(LocalDateTime.of(2026, 6, 10, 10, 0), schedule.departure());
        assertEquals(LocalDateTime.of(2026, 6, 10, 11, 0), schedule.arrival());
    }

    @Test
    void resolves_register_dto_using_ui_departure_and_arrival() {
        final String dsl = """
                flight TP100 {
                    leg {
                        departure OPO 2026-12-15 10:00
                        arrival LIS 2026-12-15 11:00
                    }
                }
                """;
        final RegisterFlightPlanDTO dto = new RegisterFlightPlanDTO(
                "TP", 100, null,
                "P001",
                LocalDateTime.of(2026, 12, 15, 10, 0),
                LocalDateTime.of(2026, 12, 15, 11, 0),
                5000, "kg",
                dsl,
                List.of(),
                null);

        final FlightPlanSchedule schedule = FlightPlanScheduleResolver.fromRegisterDto(dto, dsl);

        assertEquals(LocalDateTime.of(2026, 12, 15, 10, 0), schedule.departure());
        assertEquals(LocalDateTime.of(2026, 12, 15, 11, 0), schedule.arrival());
    }

    @Test
    void resolves_from_register_dto_with_legs() {
        final RegisterFlightPlanDTO dto = RegisterFlightPlanTestFixtures.dtoWithLegs();
        final FlightPlanSchedule schedule =
                FlightPlanScheduleResolver.fromRegisterDto(dto, null);

        assertEquals(LocalDateTime.of(2026, 6, 15, 10, 0), schedule.departure());
        assertEquals(LocalDateTime.of(2026, 6, 15, 11, 0), schedule.arrival());
    }

    private static DslFlightPlan samplePlan() {
        final DslLeg leg = new DslLeg(
                new DslSchedulePoint("OPO", "2026-06-10", "10:00", 1),
                new DslSchedulePoint("LIS", "2026-06-10", "11:00", 2),
                new DslRoute("OPO", "LIS", 3),
                List.of(new DslSegment(
                        new DslCoordinates(41.0, -8.0),
                        new DslCoordinates(38.0, -9.0),
                        10000,
                        4)),
                new DslFuel(5000, "kg", 5),
                1);
        return new DslFlightPlan("TP123", FlightType.REGULAR, List.of(leg), 1);
    }
}
