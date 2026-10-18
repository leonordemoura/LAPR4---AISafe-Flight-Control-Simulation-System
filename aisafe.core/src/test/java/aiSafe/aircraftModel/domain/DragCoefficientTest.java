package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DragCoefficientTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureDragCoefficientIsCreatedSuccessfullyWithValidData() {

        final var coefficient = new DragCoefficient(0.03);

        assertNotNull(coefficient);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeDragCoefficientIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new DragCoefficient(-0.01);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureZeroDragCoefficientIsAccepted() {

        final var coefficient = new DragCoefficient(0.0);

        assertEquals(0.0, coefficient.value());
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureValueGetterReturnsCorrectValue() {

        final var coefficient = new DragCoefficient(0.045);

        assertEquals(0.045, coefficient.value());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var coefficient1 = new DragCoefficient(0.03);
        final var coefficient2 = new DragCoefficient(0.03);

        assertEquals(coefficient1, coefficient2);
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var coefficient1 = new DragCoefficient(0.03);
        final var coefficient2 = new DragCoefficient(0.05);

        assertNotEquals(coefficient1, coefficient2);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var coefficient1 = new DragCoefficient(0.03);
        final var coefficient2 = new DragCoefficient(0.03);

        assertEquals(coefficient1.hashCode(), coefficient2.hashCode());
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var coefficient = new DragCoefficient(0.03);

        assertNotEquals(coefficient, "not a coefficient");
    }
}