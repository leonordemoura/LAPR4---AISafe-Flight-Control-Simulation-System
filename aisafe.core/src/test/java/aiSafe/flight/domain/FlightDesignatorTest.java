package aiSafe.flight.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightDesignatorTest {

    @Test
    void ensureFlightDesignatorIsCreatedSuccessfully() {
        assertNotNull(new FlightDesignator("TP", 1234, null));
    }

    @Test
    void ensureStaticFactoryOfCreatesDesignatorSuccessfully() {
        assertNotNull(FlightDesignator.of("TP", 1234, null));
    }

    @Test
    void ensureToStringReturnsCorrectValues() {
        assertEquals("TP1234", new FlightDesignator("tp", 1234, null).toString());
        assertEquals("TP1234A", new FlightDesignator("TP", 1234, "a").toString());
    }

    @Test
    void ensureGettersReturnCorrectValues() {
        final FlightDesignator d = new FlightDesignator("TP", 1234, "A");
        assertEquals("TP", d.airlineCode());
        assertEquals(1234, d.flightNumber());
        assertEquals("A", d.operationalSuffix());
    }

    @Test
    void ensureAirlineCodeValidation() {
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator(null, 1234, null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("", 1234, null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("T", 1234, null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("TAP", 1234, null));
    }

    @Test
    void ensureFlightNumberValidation() {
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("TP", 0, null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("TP", -1, null));
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("TP", 10000, null));
    }

    @Test
    void ensureOperationalSuffixValidation() {
        assertThrows(IllegalArgumentException.class, () -> new FlightDesignator("TP", 1234, "AB"));
    }

    @Test
    void orm_constructor_can_be_invoked() throws Exception {
        final var ctor = FlightDesignator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }

    @Test
    void ensureEquality() {
        final FlightDesignator a = new FlightDesignator("TP", 1234, null);
        final FlightDesignator b = new FlightDesignator("TP", 1234, null);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new FlightDesignator("FR", 1234, null));
        assertNotEquals(a, new FlightDesignator("TP", 5678, null));
        assertNotEquals(a, new FlightDesignator("TP", 1234, "A"));
        assertNotEquals(a, "TP1234");
        assertNotEquals(a, null);
    }
}