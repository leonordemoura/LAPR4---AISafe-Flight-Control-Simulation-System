package aiSafe.aircraft.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftRegistrationTest {

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAircraftRegistrationIsCreatedSuccessfully() {

        final var reg = AircraftRegistration.valueOf("CS-TUA");

        assertNotNull(reg);
        assertEquals("CS-TUA", reg.value());
    }

    // =========================================================
    // TEST 2 - NULL
    // =========================================================

    @Test
    void ensureNullRegistrationIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration.valueOf(null);
        });
    }

    // =========================================================
    // TEST 3 - EMPTY
    // =========================================================

    @Test
    void ensureEmptyRegistrationIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration.valueOf("");
        });
    }

    // =========================================================
    // TEST 4 - BLANK (only whitespace)
    // =========================================================

    @Test
    void ensureBlankRegistrationIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            AircraftRegistration.valueOf("   ");
        });
    }

    // =========================================================
    // TEST 5 - UPPERCASE NORMALISATION
    // =========================================================

    @Test
    void ensureValueIsStoredInUpperCase() {

        final var reg = AircraftRegistration.valueOf("cs-tua");

        assertEquals("CS-TUA", reg.value());
    }

    // =========================================================
    // TEST 6 - TRIM NORMALISATION
    // =========================================================

    @Test
    void ensureValueIsTrimmed() {

        final var reg = AircraftRegistration.valueOf("  CS-TUA  ");

        assertEquals("CS-TUA", reg.value());
    }

    // =========================================================
    // TEST 7 - EQUALS TRUE
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameRegistration() {

        final var reg1 = AircraftRegistration.valueOf("CS-TUA");
        final var reg2 = AircraftRegistration.valueOf("CS-TUA");

        assertEquals(reg1, reg2);
    }

    // =========================================================
    // TEST 8 - EQUALS TRUE (case insensitive input)
    // =========================================================

    @Test
    void ensureEqualsReturnsTrueForSameRegistrationDifferentCase() {

        final var reg1 = AircraftRegistration.valueOf("cs-tua");
        final var reg2 = AircraftRegistration.valueOf("CS-TUA");

        assertEquals(reg1, reg2);
    }

    // =========================================================
    // TEST 9 - EQUALS FALSE
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentRegistrations() {

        final var reg1 = AircraftRegistration.valueOf("CS-TUA");
        final var reg2 = AircraftRegistration.valueOf("CS-TUB");

        assertNotEquals(reg1, reg2);
    }

    // =========================================================
    // TEST 10 - EQUALS FALSE (non-AircraftRegistration object)
    // =========================================================

    @Test
    void ensureEqualsReturnsFalseForDifferentType() {

        final var reg = AircraftRegistration.valueOf("CS-TUA");

        assertNotEquals(reg, "CS-TUA");
    }

    // =========================================================
    // TEST 11 - HASHCODE
    // =========================================================

    @Test
    void ensureHashCodeIsConsistentWithEquals() {

        final var reg1 = AircraftRegistration.valueOf("CS-TUA");
        final var reg2 = AircraftRegistration.valueOf("CS-TUA");

        assertEquals(reg1.hashCode(), reg2.hashCode());
    }

    // =========================================================
    // TEST 12 - COMPARE TO
    // =========================================================

    @Test
    void ensureCompareToWorksCorrectly() {

        final var reg1 = AircraftRegistration.valueOf("CS-TAA");
        final var reg2 = AircraftRegistration.valueOf("CS-TZZ");

        assertTrue(reg1.compareTo(reg2) < 0);
        assertTrue(reg2.compareTo(reg1) > 0);
        assertEquals(0, reg1.compareTo(AircraftRegistration.valueOf("CS-TAA")));
    }

    // =========================================================
    // TEST 13 - TOSTRING
    // =========================================================

    @Test
    void ensureToStringReturnsTheRegistrationValue() {

        final var reg = AircraftRegistration.valueOf("CS-TUA");

        assertEquals("CS-TUA", reg.toString());
    }
}