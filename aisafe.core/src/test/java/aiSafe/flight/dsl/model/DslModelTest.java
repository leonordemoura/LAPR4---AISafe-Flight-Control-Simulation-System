package aiSafe.flight.dsl.model;

import aiSafe.flight.domain.FlightType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DslModelTest {

    private static DslLeg sampleLeg() {
        return new DslLeg(
                new DslSchedulePoint("OPO", "2026-05-11", "08:00", 1),
                new DslSchedulePoint("LIS", "2026-05-11", "09:00", 2),
                new DslRoute("OPO", "LIS", 3),
                List.of(new DslSegment(
                        new DslCoordinates(41.0, -8.0),
                        new DslCoordinates(38.0, -9.0),
                        10000,
                        4)),
                new DslFuel(5000, "kg", 5),
                1);
    }

    @Test
    void dslCoordinates_accessors_equals_hashCode_toString_and_samePosition() {
        final DslCoordinates a = new DslCoordinates(41.0, -8.0);
        final DslCoordinates b = new DslCoordinates(41.0, -8.0);
        final DslCoordinates c = new DslCoordinates(40.0, -8.0);

        assertEquals(41.0, a.latitude());
        assertEquals(-8.0, a.longitude());
        assertTrue(a.samePosition(b));
        assertFalse(a.samePosition(c));
        assertFalse(a.samePosition(null));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertTrue(a.toString().contains("41.0"));
    }

    @Test
    void dslSchedulePoint_accessors_equals_hashCode() {
        final DslSchedulePoint a = new DslSchedulePoint("OPO", "2026-05-11", "08:00", 1);
        final DslSchedulePoint b = new DslSchedulePoint("OPO", "2026-05-11", "08:00", 1);

        assertEquals("OPO", a.airportCode());
        assertEquals("2026-05-11", a.date());
        assertEquals("08:00", a.time());
        assertEquals(1, a.line());
        assertEquals(a, b);
        assertNotEquals(a, new DslSchedulePoint("LIS", "2026-05-11", "08:00", 1));
    }

    @Test
    void dslRoute_accessors_equals_hashCode() {
        final DslRoute a = new DslRoute("OPO", "LIS", 1);
        final DslRoute b = new DslRoute("OPO", "LIS", 1);

        assertEquals("OPO", a.origin());
        assertEquals("LIS", a.destination());
        assertEquals(a, b);
        assertNotEquals(a, new DslRoute("OPO", "MAD", 1));
    }

    @Test
    void dslSegment_accessors_equals_hashCode() {
        final DslCoordinates start = new DslCoordinates(1, 2);
        final DslCoordinates end = new DslCoordinates(3, 4);
        final DslSegment a = new DslSegment(start, end, 10000, 1);
        final DslSegment b = new DslSegment(start, end, 10000, 1);

        assertSame(start, a.start());
        assertSame(end, a.end());
        assertEquals(10000, a.altitudeMeters());
        assertEquals(a, b);
        assertNotEquals(a, new DslSegment(start, end, 9000, 1));
    }

    @Test
    void dslFuel_accessors_equals_hashCode() {
        final DslFuel a = new DslFuel(5000, "kg", 1);
        final DslFuel b = new DslFuel(5000, "kg", 1);

        assertEquals(5000, a.amount());
        assertEquals("kg", a.unit());
        assertEquals(a, b);
        assertNotEquals(a, new DslFuel(100, "kg", 1));
    }

    @Test
    void dslLeg_accessors_equals_hashCode_and_unmodifiable_segments() {
        final DslLeg leg = sampleLeg();
        final DslLeg same = sampleLeg();

        assertEquals("OPO", leg.departure().airportCode());
        assertEquals("LIS", leg.arrival().airportCode());
        assertEquals(1, leg.segments().size());
        assertThrows(UnsupportedOperationException.class, () -> leg.segments().add(leg.segments().getFirst()));
        assertEquals(leg, same);
        assertNotEquals(leg, new DslLeg(
                leg.departure(), leg.arrival(), leg.route(), leg.segments(), leg.fuel(), 99));
    }

    @Test
    void dslValueObjects_equals_distinguish_all_fields() {
        final DslSchedulePoint dep = new DslSchedulePoint("OPO", "2026-01-01", "08:00", 1);
        final DslSchedulePoint arr = new DslSchedulePoint("LIS", "2026-01-01", "09:00", 2);
        final DslRoute route = new DslRoute("OPO", "LIS", 3);
        final DslSegment seg = new DslSegment(
                new DslCoordinates(1, 2), new DslCoordinates(3, 4), 1000, 4);
        final DslFuel fuel = new DslFuel(100, "kg", 5);

        assertNotEquals(dep, new DslSchedulePoint("MAD", "2026-01-01", "08:00", 1));
        assertNotEquals(dep, new DslSchedulePoint("OPO", "2026-01-02", "08:00", 1));
        assertNotEquals(dep, new DslSchedulePoint("OPO", "2026-01-01", "09:00", 1));
        assertNotEquals(arr, new DslSchedulePoint("LIS", "2026-01-02", "09:00", 2));
        assertEquals(route, route);
        assertNotEquals(route, "not-a-route");
        assertNotEquals(route, new DslRoute("OPO", "MAD", 3));
        assertNotEquals(route, new DslRoute("MAD", "LIS", 3));
        assertNotEquals(seg, new DslSegment(
                new DslCoordinates(9, 2), new DslCoordinates(3, 4), 1000, 4));
        assertNotEquals(seg, new DslSegment(
                new DslCoordinates(1, 2), new DslCoordinates(3, 4), 500, 4));
        assertNotEquals(fuel, new DslFuel(100, "l", 5));
        assertNotEquals(fuel, new DslFuel(50, "kg", 5));

        final DslLeg leg = new DslLeg(dep, arr, route, List.of(seg), fuel, 1);
        assertEquals(leg, leg);
        assertNotEquals(leg, "not-a-leg");
        assertNotEquals(leg, new DslLeg(dep, arr, route, List.of(seg), fuel, 99));
        assertNotEquals(leg, new DslLeg(
                new DslSchedulePoint("MAD", "2026-01-01", "08:00", 1),
                arr, route, List.of(seg), fuel, 1));
        assertNotEquals(leg, new DslLeg(dep,
                new DslSchedulePoint("LIS", "2026-01-01", "10:00", 2),
                route, List.of(seg), fuel, 1));
        assertNotEquals(leg, new DslLeg(dep, arr,
                new DslRoute("MAD", "LIS", 99), List.of(seg), fuel, 1));
        assertNotEquals(leg, new DslLeg(dep, arr, route, List.of(), fuel, 1));
        assertNotEquals(leg, new DslLeg(dep, arr, route, List.of(seg),
                new DslFuel(1, "kg", 99), 1));
    }

    @Test
    void dslFlightPlan_accessors_equals_hashCode_toString_and_unmodifiable_legs() {
        final DslFlightPlan plan = new DslFlightPlan("TP123", FlightType.REGULAR, List.of(sampleLeg()), 1);
        final DslFlightPlan same = new DslFlightPlan("TP123", FlightType.REGULAR, List.of(sampleLeg()), 1);

        assertEquals(plan, plan);
        assertNotEquals(plan, "not-a-plan");
        assertEquals("TP123", plan.identifier());
        assertEquals(FlightType.REGULAR, plan.flightType());
        assertEquals(1, plan.line());
        assertEquals(1, plan.legs().size());
        assertThrows(UnsupportedOperationException.class, () -> plan.legs().add(sampleLeg()));
        assertEquals(plan, same);
        assertNotEquals(plan, new DslFlightPlan("TP999", FlightType.CHARTER, List.of(sampleLeg()), 1));
        assertNotEquals(plan, new DslFlightPlan("TP123", FlightType.CHARTER, List.of(sampleLeg()), 1));
        assertNotEquals(plan, new DslFlightPlan("TP123", FlightType.REGULAR, List.of(sampleLeg()), 99));
        assertTrue(plan.toString().contains("TP123"));
    }
}
