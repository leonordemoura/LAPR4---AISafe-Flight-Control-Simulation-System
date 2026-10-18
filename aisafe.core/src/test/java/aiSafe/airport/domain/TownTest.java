package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TownTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureTownIsCreatedSuccessfully() {

        final var town = new Town("Porto");

        assertNotNull(town);
    }

    @Test
    void ensureValueReturnsCorrectTown() {

        final var town = new Town("Porto");

        assertEquals("Porto", town.value());
    }

    @Test
    void ensureToStringReturnsCorrectTown() {

        final var town = new Town("Porto");

        assertEquals("Porto", town.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureTownCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Town(null);
        });
    }

    @Test
    void ensureTownCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Town("");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameTownsAreEqual() {

        final var town1 = new Town("Porto");
        final var town2 = new Town("Porto");

        assertEquals(town1, town2);
    }

    @Test
    void ensureDifferentTownsAreNotEqual() {

        final var town1 = new Town("Porto");
        final var town2 = new Town("Lisboa");

        assertNotEquals(town1, town2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValue() {

        final var town1 = new Town("Porto");
        final var town2 = new Town("Porto");

        assertEquals(town1.hashCode(), town2.hashCode());
    }

    @Test
    void ensureTownIsNotEqualToDifferentObjectType() {

        final var town = new Town("Porto");

        assertNotEquals(town, "Porto");
    }

    @Test
    void ensureTownIsEqualToItself() {

        final var town = new Town("Porto");

        assertEquals(town, town);
    }
}