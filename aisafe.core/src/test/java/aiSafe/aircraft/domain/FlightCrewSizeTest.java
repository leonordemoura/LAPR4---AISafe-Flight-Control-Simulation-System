package aiSafe.aircraft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightCrewSizeTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureFlightCrewSizeIsCreatedSuccessfully() {

        final var crewSize = FlightCrewSize.valueOf(2);

        assertNotNull(crewSize);
        assertEquals(2, crewSize.value());
    }

    // =========================================================
    // TEST 2 - ZERO
    // =========================================================

    @Test
    void ensureZeroCrewSizeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            FlightCrewSize.valueOf(0);
        });
    }

    // =========================================================
    // TEST 3 - NEGATIVE
    // =========================================================

    @Test
    void ensureNegativeCrewSizeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            FlightCrewSize.valueOf(-1);
        });
    }

    // =========================================================
    // TEST 4 - BOUNDARY (minimum valid value)
    // =========================================================

    @Test
    void ensureCrewSizeOfOneIsAllowed() {

        final var crewSize = FlightCrewSize.valueOf(1);

        assertEquals(1, crewSize.value());
    }

    // =========================================================
    // TEST 5 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameCrewSize() {

        final var crewSize1 = FlightCrewSize.valueOf(3);
        final var crewSize2 = FlightCrewSize.valueOf(3);

        assertEquals(crewSize1, crewSize2);
    }

    // =========================================================
    // TEST 6 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentCrewSizes() {

        final var crewSize1 = FlightCrewSize.valueOf(2);
        final var crewSize2 = FlightCrewSize.valueOf(5);

        assertNotEquals(crewSize1, crewSize2);
    }

    // =========================================================
    // TEST 7 - EQUALS FALSE (non-FlightCrewSize object)
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var crewSize = FlightCrewSize.valueOf(2);

        assertNotEquals(crewSize, 2);
    }

    // =========================================================
    // TEST 8 - EQUALS FALSE (null)
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForNull() {

        final var crewSize = FlightCrewSize.valueOf(2);

        assertNotEquals(crewSize, null);
    }

    // =========================================================
    // TEST 9 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var crewSize1 = FlightCrewSize.valueOf(3);
        final var crewSize2 = FlightCrewSize.valueOf(3);

        assertEquals(crewSize1.hashCode(), crewSize2.hashCode());
    }
}