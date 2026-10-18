package aiSafe.flight.application;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanReplaceabilityGuardTest {

    @Test
    void ensureReplaceable_rejects_validated_plan() throws Exception {
        final Flight flight = flightWithPlan(FlightPlanStatus.VALIDATED);

        final IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> FlightPlanReplaceabilityGuard.ensureReplaceable(flight));

        assertTrue(ex.getMessage().contains("VALIDATED"));
        assertTrue(ex.getMessage().contains("TP123"));
    }

    @Test
    void ensureReplaceable_allows_draft_plan() throws Exception {
        assertDoesNotThrow(() -> FlightPlanReplaceabilityGuard.ensureReplaceable(flightWithPlan(FlightPlanStatus.DRAFT)));
    }

    @Test
    void replacementNotice_describes_draft_replacement() throws Exception {
        final var notice = FlightPlanReplaceabilityGuard.replacementNotice(flightWithPlan(FlightPlanStatus.DRAFT));

        assertTrue(notice.isPresent());
        assertTrue(notice.get().contains("DRAFT"));
        assertTrue(notice.get().contains("replace"));
    }

    private static Flight flightWithPlan(final FlightPlanStatus status) throws Exception {
        final Flight flight = new Flight(
                FlightRoute.valueOf(RouteName.valueOf("TP", 123), RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
        final FlightPlan plan = flight.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new aiSafe.PilotCertification.domain.PilotID("P001"),
                new DepartureDateTime(LocalDateTime.parse("2026-07-07T10:00")),
                new ArrivalDateTime(
                        LocalDateTime.parse("2026-07-07T11:00"),
                        LocalDateTime.parse("2026-07-07T10:00")),
                new FuelQuantity(3200, "kg"),
                new DslSource("flight TP123 { type REGULAR }"));
        plan.applyRegistrationMetadata("TP123", "OPO", "LIS", 1);
        setStatus(plan, status);
        return flight;
    }

    private static void setStatus(final FlightPlan plan, final FlightPlanStatus status) throws Exception {
        final Field field = FlightPlan.class.getDeclaredField("status");
        field.setAccessible(true);
        field.set(plan, status);
    }
}
