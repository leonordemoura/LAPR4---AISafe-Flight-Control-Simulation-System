package aiSafe.PilotCertification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PilotIDTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensurePilotIDIsCreatedSuccessfully() {

        final var id = new PilotID("P-001");

        assertNotNull(id);
        assertEquals("P-001", id.value());
        assertEquals("P-001", id.toString());
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNullPilotIDIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new PilotID(null)
        );
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureEmptyPilotIDIsRejected() {

        assertThrows(IllegalArgumentException.class, () ->
                new PilotID("")
        );
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureSameValuePilotIDsAreEqual() {

        final var id1 = new PilotID("P-001");
        final var id2 = new PilotID("P-001");

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureDifferentPilotIDsAreNotEqual() {

        final var id1 = new PilotID("P-001");
        final var id2 = new PilotID("P-002");

        assertNotEquals(id1, id2);
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureCompareToWorksCorrectly() {

        final var id1 = new PilotID("P-001");
        final var id2 = new PilotID("P-002");

        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureSameObjectIsEqual() {

        final var id = new PilotID("P-001");

        assertEquals(id, id);
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var id = new PilotID("P-001");

        assertNotEquals(id, "P-001");
    }
}