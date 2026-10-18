package aiSafe.aircraft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrentEngineTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureCurrentEngineIsCreatedSuccessfully() {

        final var engine = CurrentEngine.valueOf("CFM56-7B");

        assertNotNull(engine);
        assertEquals("CFM56-7B", engine.value());
    }

    // =========================================================
    // TEST 2 - NULL
    // =========================================================

    @Test
    void ensureNullEngineModelIdIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            CurrentEngine.valueOf(null);
        });
    }

    // =========================================================
    // TEST 3 - EMPTY
    // =========================================================

    @Test
    void ensureEmptyEngineModelIdIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            CurrentEngine.valueOf("");
        });
    }

    // =========================================================
    // TEST 4 - BLANK (only whitespace)
    // =========================================================

    @Test
    void ensureBlankEngineModelIdIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            CurrentEngine.valueOf("   ");
        });
    }

    // =========================================================
    // TEST 5 - TRIM NORMALISATION
    // =========================================================

    @Test
    void ensureValueIsTrimmed() {

        final var engine = CurrentEngine.valueOf("  CFM56-7B  ");

        assertEquals("CFM56-7B", engine.value());
    }

    // =========================================================
    // TEST 6 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameEngineModelId() {

        final var engine1 = CurrentEngine.valueOf("CFM56-7B");
        final var engine2 = CurrentEngine.valueOf("CFM56-7B");

        assertEquals(engine1, engine2);
    }

    // =========================================================
    // TEST 7 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentEngineModelIds() {

        final var engine1 = CurrentEngine.valueOf("CFM56-7B");
        final var engine2 = CurrentEngine.valueOf("GE90-115B");

        assertNotEquals(engine1, engine2);
    }

    // =========================================================
    // TEST 8 - EQUALS FALSE (non-CurrentEngine object)
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var engine = CurrentEngine.valueOf("CFM56-7B");

        assertNotEquals(engine, "CFM56-7B");
    }

    // =========================================================
    // TEST 9 - EQUALS FALSE (null)
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForNull() {

        final var engine = CurrentEngine.valueOf("CFM56-7B");

        assertNotEquals(engine, null);
    }

    // =========================================================
    // TEST 10 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var engine1 = CurrentEngine.valueOf("CFM56-7B");
        final var engine2 = CurrentEngine.valueOf("CFM56-7B");

        assertEquals(engine1.hashCode(), engine2.hashCode());
    }

    // =========================================================
    // TEST 11 - TOSTRING
    // =========================================================

    @Test
    void ensureToStringReturnsEngineModelId() {

        final var engine = CurrentEngine.valueOf("CFM56-7B");

        assertEquals("CFM56-7B", engine.toString());
    }
}