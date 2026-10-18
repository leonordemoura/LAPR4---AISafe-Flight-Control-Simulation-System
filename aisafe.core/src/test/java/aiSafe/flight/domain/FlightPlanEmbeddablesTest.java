package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanEmbeddablesTest {

    @Test
    void fuelQuantity_value_and_unit_accessors() {
        final FuelQuantity fuel = new FuelQuantity(1500.5, "  kg  ");
        assertEquals(1500.5, fuel.value());
        assertEquals("kg", fuel.unit());
    }

    @Test
    void fuelQuantity_equals_and_hashCode() {
        final FuelQuantity a = new FuelQuantity(1000, "KG");
        final FuelQuantity b = new FuelQuantity(1000, "KG");
        final FuelQuantity c = new FuelQuantity(2000, "KG");

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, "not-fuel");
    }

    @Test
    void fuelQuantity_orm_constructor() throws Exception {
        final Constructor<FuelQuantity> ctor = FuelQuantity.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void departureDateTime_value_accessor() {
        final LocalDateTime when = LocalDateTime.now().plusDays(2);
        assertEquals(when, new DepartureDateTime(when).value());
    }

    @Test
    void departureDateTime_rejects_null() {
        assertThrows(IllegalArgumentException.class, () -> new DepartureDateTime(null));
    }

    @Test
    void departureDateTime_rejects_past_instant() {
        assertThrows(IllegalArgumentException.class,
                () -> new DepartureDateTime(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void departureDateTime_equals_and_hashCode() {
        final LocalDateTime when = LocalDateTime.now().plusDays(3);
        final DepartureDateTime a = new DepartureDateTime(when);
        final DepartureDateTime b = new DepartureDateTime(when);
        final DepartureDateTime c = new DepartureDateTime(when.plusHours(1));

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void departureDateTime_orm_constructor() throws Exception {
        final Constructor<DepartureDateTime> ctor = DepartureDateTime.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void pilotId_stores_value() {
        final PilotID id = new PilotID("P-001");
        assertEquals("P-001", id.value());
        assertEquals("P-001", id.toString());
    }

    @Test
    void pilotId_equals_and_hashCode() {
        final PilotID a = new PilotID("P-001");
        final PilotID b = new PilotID("P-002");

        assertEquals(a, a);
        assertEquals(a, new PilotID("P-001"));
        assertNotEquals(a, b);
    }

    @Test
    void pilotId_rejects_null_or_blank() {
        assertThrows(Exception.class, () -> new PilotID(null));
        assertThrows(Exception.class, () -> new PilotID(""));
    }

    @Test
    void pilotId_orm_constructor() throws Exception {
        final Constructor<PilotID> ctor = PilotID.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}
