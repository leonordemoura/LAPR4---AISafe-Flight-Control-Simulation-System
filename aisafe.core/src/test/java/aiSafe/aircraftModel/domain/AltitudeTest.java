package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AltitudeTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAltitudeIsCreatedSuccessfully() {

        final var altitude = new Altitude(12000, AltitudeUnit.FEET);

        assertNotNull(altitude);
        assertEquals(12000, altitude.value());
        assertEquals(AltitudeUnit.FEET, altitude.unit());
    }

    // =========================================================
    // TEST 2 - NEGATIVE VALUE
    // =========================================================

    @Test
    void ensureNegativeAltitudeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Altitude(-100, AltitudeUnit.FEET);
        });
    }

    // =========================================================
    // TEST 3 - NULL UNIT
    // =========================================================

    @Test
    void ensureNullUnitIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Altitude(1000, null);
        });
    }

    // =========================================================
    // TEST 4 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var altitude1 = new Altitude(12000, AltitudeUnit.FEET);
        final var altitude2 = new Altitude(12000, AltitudeUnit.FEET);

        assertEquals(altitude1, altitude2);
    }

    // =========================================================
    // TEST 5 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var altitude1 = new Altitude(12000, AltitudeUnit.FEET);
        final var altitude2 = new Altitude(15000, AltitudeUnit.FEET);

        assertNotEquals(altitude1, altitude2);
    }

    // =========================================================
    // TEST 6 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var altitude1 = new Altitude(12000, AltitudeUnit.FEET);
        final var altitude2 = new Altitude(12000, AltitudeUnit.FEET);

        assertEquals(altitude1.hashCode(), altitude2.hashCode());
    }

    // =========================================================
    // TEST 7 - DIFFERENT UNITS
    // =========================================================

    @Test
    void ensureDifferentUnitsAreNotEqual() {

        final var altitude1 = new Altitude(12000, AltitudeUnit.FEET);
        final var altitude2 = new Altitude(12000, AltitudeUnit.METERS);

        assertNotEquals(altitude1, altitude2);
    }
}