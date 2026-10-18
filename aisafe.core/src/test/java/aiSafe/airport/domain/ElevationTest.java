package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ElevationTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureElevationIsCreatedSuccessfully() {

        final var elevation = new Elevation(70.0);

        assertNotNull(elevation);
    }

    @Test
    void ensureValueReturnsCorrectElevation() {

        final var elevation = new Elevation(70.0);

        assertEquals(70.0, elevation.value());
    }

    @Test
    void ensureToStringReturnsCorrectElevation() {

        final var elevation = new Elevation(70.0);

        assertEquals("70.0", elevation.toString());
    }

    // =========================================================
    // NEGATIVE ELEVATION
    // =========================================================

    @Test
    void ensureNegativeElevationIsAccepted() {

        assertDoesNotThrow(() -> {
            new Elevation(-3.0);
        });
    }

    @Test
    void ensureZeroElevationIsAccepted() {

        assertDoesNotThrow(() -> {
            new Elevation(0.0);
        });
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureElevationCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Elevation(null);
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureElevationsWithSameValueAreEqual() {

        final var elevation1 = new Elevation(70.0);
        final var elevation2 = new Elevation(70.0);

        assertEquals(elevation1, elevation2);
    }

    @Test
    void ensureElevationsWithDifferentValuesAreNotEqual() {

        final var elevation1 = new Elevation(70.0);
        final var elevation2 = new Elevation(100.0);

        assertNotEquals(elevation1, elevation2);
    }

    @Test
    void ensureHashCodeIsEqualForSameElevation() {

        final var elevation1 = new Elevation(70.0);
        final var elevation2 = new Elevation(70.0);

        assertEquals(elevation1.hashCode(), elevation2.hashCode());
    }

    @Test
    void ensureElevationIsNotEqualToDifferentObjectType() {

        final var elevation = new Elevation(70.0);

        assertNotEquals(elevation, "70.0");
    }

    @Test
    void ensureElevationIsEqualToItself() {

        final var elevation = new Elevation(70.0);

        assertEquals(elevation, elevation);
    }
}