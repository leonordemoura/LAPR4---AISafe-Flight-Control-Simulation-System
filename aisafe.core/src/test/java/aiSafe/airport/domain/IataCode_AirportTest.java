package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IataCode_AirportTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureIataCodeIsCreatedSuccessfully() {

        final var code = new IataCode_Airport("OPO");

        assertNotNull(code);
    }

    @Test
    void ensureValueReturnsCorrectIataCode() {

        final var code = new IataCode_Airport("OPO");

        assertEquals("OPO", code.value());
    }

    @Test
    void ensureToStringReturnsCorrectIataCode() {

        final var code = new IataCode_Airport("OPO");

        assertEquals("OPO", code.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureIataCodeCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport(null);
        });
    }

    @Test
    void ensureIataCodeMustHaveExactlyThreeLetters() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("OP"); // 2 letters
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("OPOR"); // 4 letters
        });
    }

    @Test
    void ensureIataCodeMustBeUppercaseOnly() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("opo");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("OpO");
        });
    }

    @Test
    void ensureIataCodeCannotContainNumbers() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("O1O");
        });
    }

    @Test
    void ensureEmptyIataCodeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IataCode_Airport("");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameIataCodesAreEqual() {

        final var code1 = new IataCode_Airport("OPO");
        final var code2 = new IataCode_Airport("OPO");

        assertEquals(code1, code2);
    }

    @Test
    void ensureDifferentIataCodesAreNotEqual() {

        final var code1 = new IataCode_Airport("OPO");
        final var code2 = new IataCode_Airport("LIS");

        assertNotEquals(code1, code2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValues() {

        final var code1 = new IataCode_Airport("OPO");
        final var code2 = new IataCode_Airport("OPO");

        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    void ensureIataCodeIsNotEqualToDifferentObjectType() {

        final var code = new IataCode_Airport("OPO");

        assertNotEquals(code, "OPO");
    }

    @Test
    void ensureIataCodeIsEqualToItself() {

        final var code = new IataCode_Airport("OPO");

        assertEquals(code, code);
    }
}