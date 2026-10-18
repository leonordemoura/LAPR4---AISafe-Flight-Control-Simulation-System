package aiSafe.airport.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcaoCode_AirportTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureIcaoCodeIsCreatedSuccessfully() {

        final var code = new IcaoCode_Airport("LPPR");

        assertNotNull(code);
    }

    @Test
    void ensureValueReturnsCorrectIcaoCode() {

        final var code = new IcaoCode_Airport("LPPR");

        assertEquals("LPPR", code.value());
    }

    @Test
    void ensureToStringReturnsCorrectIcaoCode() {

        final var code = new IcaoCode_Airport("LPPR");

        assertEquals("LPPR", code.toString());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureIcaoCodeCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport(null);
        });
    }

    @Test
    void ensureIcaoCodeMustHaveExactlyFourLetters() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("LPP"); // 3 letters
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("LPPRR"); // 5 letters
        });
    }

    @Test
    void ensureIcaoCodeMustBeUppercaseOnly() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("lppr");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("LpPr");
        });
    }

    @Test
    void ensureIcaoCodeCannotContainNumbers() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("LP1R");
        });
    }

    @Test
    void ensureEmptyIcaoCodeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoCode_Airport("");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameIcaoCodesAreEqual() {

        final var code1 = new IcaoCode_Airport("LPPR");
        final var code2 = new IcaoCode_Airport("LPPR");

        assertEquals(code1, code2);
    }

    @Test
    void ensureDifferentIcaoCodesAreNotEqual() {

        final var code1 = new IcaoCode_Airport("LPPR");
        final var code2 = new IcaoCode_Airport("LPPT");

        assertNotEquals(code1, code2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValues() {

        final var code1 = new IcaoCode_Airport("LPPR");
        final var code2 = new IcaoCode_Airport("LPPR");

        assertEquals(code1.hashCode(), code2.hashCode());
    }

    @Test
    void ensureIcaoCodeIsNotEqualToDifferentObjectType() {

        final var code = new IcaoCode_Airport("LPPR");

        assertNotEquals(code, "LPPR");
    }

    @Test
    void ensureIcaoCodeIsEqualToItself() {

        final var code = new IcaoCode_Airport("LPPR");

        assertEquals(code, code);
    }
}