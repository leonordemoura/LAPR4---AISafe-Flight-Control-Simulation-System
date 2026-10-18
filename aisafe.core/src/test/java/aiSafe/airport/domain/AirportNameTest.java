package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirportNameTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureAirportNameIsCreatedSuccessfully() {

        final var airportName = new AirportName("Porto Airport");

        assertNotNull(airportName);
    }

    @Test
    void ensureValueReturnsCorrectAirportName() {

        final var airportName = new AirportName("Porto Airport");

        assertEquals("Porto Airport", airportName.value());
    }

    @Test
    void ensureToStringReturnsCorrectAirportName() {

        final var airportName = new AirportName("Porto Airport");

        assertEquals("Porto Airport", airportName.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureAirportNameCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AirportName(null);
        });
    }

    @Test
    void ensureAirportNameCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AirportName("");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureTwoAirportNamesWithSameValueAreEqual() {

        final var name1 = new AirportName("Porto Airport");
        final var name2 = new AirportName("Porto Airport");

        assertEquals(name1, name2);
    }

    @Test
    void ensureTwoAirportNamesWithDifferentValuesAreNotEqual() {

        final var name1 = new AirportName("Porto Airport");
        final var name2 = new AirportName("Lisbon Airport");

        assertNotEquals(name1, name2);
    }

    @Test
    void ensureHashCodeIsEqualForSameAirportName() {

        final var name1 = new AirportName("Porto Airport");
        final var name2 = new AirportName("Porto Airport");

        assertEquals(name1.hashCode(), name2.hashCode());
    }

    @Test
    void ensureAirportNameIsNotEqualToDifferentObjectType() {

        final var airportName = new AirportName("Porto Airport");

        assertNotEquals(airportName, "Porto Airport");
    }

    @Test
    void ensureAirportNameIsEqualToItself() {

        final var airportName = new AirportName("Porto Airport");

        assertEquals(airportName, airportName);
    }

    @Test
    void ensureAirportNameNotEqualToNull() {

        final var name = new AirportName("Porto");

        assertNotEquals(null, name);
    }

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var name = new AirportName("Porto");

        assertFalse(name.equals(123));
    }

    @Test
    void ensureHashCodeWorksEvenWithSameValue() {

        final var n1 = new AirportName("Porto");
        final var n2 = new AirportName("Porto");

        assertEquals(n1.hashCode(), n2.hashCode());
    }

}