package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiftCoefficientTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureLiftCoefficientIsCreatedSuccessfullyWithValidData() {

        final var coefficient = new LiftCoefficient(1.25);

        assertNotNull(coefficient);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeLiftCoefficientIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new LiftCoefficient(-0.5);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureZeroLiftCoefficientIsAccepted() {

        final var coefficient = new LiftCoefficient(0.0);

        assertEquals(0.0, coefficient.value());
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureValueGetterReturnsCorrectValue() {

        final var coefficient = new LiftCoefficient(1.45);

        assertEquals(1.45, coefficient.value());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var coefficient1 = new LiftCoefficient(1.2);
        final var coefficient2 = new LiftCoefficient(1.2);

        assertEquals(coefficient1, coefficient2);
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var coefficient1 = new LiftCoefficient(1.2);
        final var coefficient2 = new LiftCoefficient(1.5);

        assertNotEquals(coefficient1, coefficient2);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var coefficient1 = new LiftCoefficient(1.2);
        final var coefficient2 = new LiftCoefficient(1.2);

        assertEquals(coefficient1.hashCode(), coefficient2.hashCode());
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var coefficient = new LiftCoefficient(1.2);

        assertNotEquals(coefficient, "not a coefficient");
    }
}