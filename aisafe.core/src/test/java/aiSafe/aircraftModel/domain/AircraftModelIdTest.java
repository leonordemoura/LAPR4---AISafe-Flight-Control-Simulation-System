package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelIdTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAircraftModelIdIsCreatedSuccessfully() {

        final var id = new AircraftModelId("MODEL-001");

        assertNotNull(id);
        assertEquals("MODEL-001", id.value());
    }

    // =========================================================
    // TEST 2 - NULL
    // =========================================================

    @Test
    void ensureNullIdIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModelId(null);
        });
    }

    // =========================================================
    // TEST 3 - EMPTY
    // =========================================================

    @Test
    void ensureEmptyIdIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModelId("");
        });
    }

    // =========================================================
    // TEST 4 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameId() {

        final var id1 = new AircraftModelId("MODEL-001");
        final var id2 = new AircraftModelId("MODEL-001");

        assertEquals(id1, id2);
    }

    // =========================================================
    // TEST 5 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentIds() {

        final var id1 = new AircraftModelId("MODEL-001");
        final var id2 = new AircraftModelId("MODEL-002");

        assertNotEquals(id1, id2);
    }

    // =========================================================
    // TEST 6 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var id1 = new AircraftModelId("MODEL-001");
        final var id2 = new AircraftModelId("MODEL-001");

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // =========================================================
    // TEST 7 - COMPARE TO
    // =========================================================

    @Test
    void ensureCompareToWorksCorrectly() {

        final var id1 = new AircraftModelId("MODEL-001");
        final var id2 = new AircraftModelId("MODEL-002");

        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(new AircraftModelId("MODEL-001")));
    }

    // =========================================================
    // TEST 8 - TOSTRING
    // =========================================================

    @Test
    void ensureToStringReturnsCorrectValue() {

        final var id = new AircraftModelId("MODEL-XYZ");

        assertEquals("MODEL-XYZ", id.toString());
    }

    @Test
    void ensureEqualsReturnsTrueForSameInstance() {

        final var id = new AircraftModelId("MODEL-001");

        assertEquals(id, id);
    }

    @Test
    void ensureEqualsReturnsFalseForNull() {

        final var id = new AircraftModelId("MODEL-001");

        assertNotEquals(id, null);
    }

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var id = new AircraftModelId("MODEL-001");

        assertNotEquals(id, "MODEL-001");
    }

    @Test
    void ensureValueReturnsCorrectId() {

        final var id = new AircraftModelId("ABC-123");

        assertEquals("ABC-123", id.value());
    }

    @Test
    void ensureCompareToWithEqualValuesReturnsZero() {

        final var id1 = new AircraftModelId("MODEL-001");
        final var id2 = new AircraftModelId("MODEL-001");

        assertEquals(0, id1.compareTo(id2));
    }

    @Test
    void ensureHashCodeIsStable() {

        final var id = new AircraftModelId("MODEL-001");

        final int first = id.hashCode();
        final int second = id.hashCode();

        assertEquals(first, second);
    }
}