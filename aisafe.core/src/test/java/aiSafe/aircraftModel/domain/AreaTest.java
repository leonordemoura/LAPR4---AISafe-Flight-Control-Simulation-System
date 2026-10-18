package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureAreaIsCreatedSuccessfullyWithValidData() {

        final var area = new Area(122.6, AreaUnit.m2);

        assertNotNull(area);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeAreaIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Area(-1.0, AreaUnit.m2);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureNullUnitIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Area(122.6, null);
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureZeroAreaIsAccepted() {

        final var area = new Area(0.0, AreaUnit.m2);

        assertEquals(0.0, area.value());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureValueGetterReturnsCorrectValue() {

        final var area = new Area(150.5, AreaUnit.m2);

        assertEquals(150.5, area.value());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var area1 = new Area(122.6, AreaUnit.m2);
        final var area2 = new Area(122.6, AreaUnit.m2);

        assertEquals(area1, area2);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var area1 = new Area(122.6, AreaUnit.m2);
        final var area2 = new Area(150.0, AreaUnit.m2);

        assertNotEquals(area1, area2);
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var area1 = new Area(122.6, AreaUnit.m2);
        final var area2 = new Area(122.6, AreaUnit.m2);

        assertEquals(area1.hashCode(), area2.hashCode());
    }
}