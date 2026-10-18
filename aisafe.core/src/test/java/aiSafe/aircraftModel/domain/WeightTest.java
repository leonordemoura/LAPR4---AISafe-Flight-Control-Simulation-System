package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightTest {

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensureWeightIsCreatedSuccessfullyWithValidData() {

        final var weight = new Weight(75000, WeightUnit.KG);

        assertNotNull(weight);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNegativeWeightIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Weight(-1, WeightUnit.KG);
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureNullUnitIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Weight(75000, null);
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureZeroWeightIsAccepted() {

        final var weight = new Weight(0, WeightUnit.KG);

        assertEquals(0, weight.value());
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureValueGetterReturnsCorrectValue() {

        final var weight = new Weight(41000, WeightUnit.KG);

        assertEquals(41000, weight.value());
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameValues() {

        final var weight1 = new Weight(50000, WeightUnit.KG);
        final var weight2 = new Weight(50000, WeightUnit.KG);

        assertEquals(weight1, weight2);
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentValues() {

        final var weight1 = new Weight(50000, WeightUnit.KG);
        final var weight2 = new Weight(70000, WeightUnit.KG);

        assertNotEquals(weight1, weight2);
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureHashCodeIsEqualForEqualObjects() {

        final var weight1 = new Weight(50000, WeightUnit.KG);
        final var weight2 = new Weight(50000, WeightUnit.KG);

        assertEquals(weight1.hashCode(), weight2.hashCode());
    }

    // =========================================================
    // TEST 9
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentObjectType() {

        final var weight = new Weight(50000, WeightUnit.KG);

        assertNotEquals(weight, "not a weight");
    }
}