package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelNameTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAircraftModelNameIsCreatedSuccessfully() {

        final var name = new AircraftModelName("A320");

        assertNotNull(name);
        assertEquals("A320", name.value());
    }

    // =========================================================
    // TEST 2 - NULL
    // =========================================================

    @Test
    void ensureNullNameIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModelName(null);
        });
    }

    // =========================================================
    // TEST 3 - EMPTY
    // =========================================================

    @Test
    void ensureEmptyNameIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModelName("");
        });
    }

    // =========================================================
    // TEST 4 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameName() {

        final var name1 = new AircraftModelName("A320");
        final var name2 = new AircraftModelName("A320");

        assertEquals(name1, name2);
    }

    // =========================================================
    // TEST 5 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentNames() {

        final var name1 = new AircraftModelName("A320");
        final var name2 = new AircraftModelName("B737");

        assertNotEquals(name1, name2);
    }

    // =========================================================
    // TEST 6 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var name1 = new AircraftModelName("A320");
        final var name2 = new AircraftModelName("A320");

        assertEquals(name1.hashCode(), name2.hashCode());
    }

    // =========================================================
    // TEST 7 - TOSTRING
    // =========================================================

    @Test
    void ensureToStringReturnsCorrectValue() {

        final var name = new AircraftModelName("A320");

        assertEquals("A320", name.toString());
    }
}