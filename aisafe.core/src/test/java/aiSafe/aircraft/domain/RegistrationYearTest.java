package aiSafe.aircraft.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationYearTest {

    private static final int CURRENT_YEAR = LocalDate.now().getYear();

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureRegistrationYearIsCreatedSuccessfully() {

        final var regYear = new RegistrationYear(2010);

        assertNotNull(regYear);
        assertEquals(2010, regYear.value());
    }

    // =========================================================
    // TEST 2 - CURRENT YEAR (boundary)
    // =========================================================

    @Test
    void ensureCurrentYearIsAllowed() {

        final var regYear = new RegistrationYear(CURRENT_YEAR);

        assertEquals(CURRENT_YEAR, regYear.value());
    }

    // =========================================================
    // TEST 3 - YEAR 1901 (minimum valid boundary)
    // =========================================================

    @Test
    void ensureMinimumValidYearIsAllowed() {

        final var regYear = new RegistrationYear(1901);

        assertEquals(1901, regYear.value());
    }

    // =========================================================
    // TEST 4 - YEAR 1900 (below minimum boundary)
    // =========================================================

    @Test
    void ensureYearOf1900IsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new RegistrationYear(1900);
        });
    }

    // =========================================================
    // TEST 5 - YEAR BEFORE 1900
    // =========================================================

    @Test
    void ensureYearBefore1900IsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new RegistrationYear(1800);
        });
    }

    // =========================================================
    // TEST 6 - FUTURE YEAR
    // =========================================================

    @Test
    void ensureFutureYearIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new RegistrationYear(CURRENT_YEAR + 1);
        });
    }

    // =========================================================
    // TEST 7 - AGE
    // =========================================================

    @Test
    void ensureAgeIsCorrectlyCalculated() {

        final int registrationYear = CURRENT_YEAR - 10;
        final var regYear = new RegistrationYear(registrationYear);

        assertEquals(10, regYear.age());
    }

    // =========================================================
    // TEST 8 - AGE ZERO (registered this year)
    // =========================================================

    @Test
    void ensureAgeIsZeroForCurrentYear() {

        final var regYear = new RegistrationYear(CURRENT_YEAR);

        assertEquals(0, regYear.age());
    }

    // =========================================================
    // TEST 9 - TOSTRING
    // =========================================================

    @Test
    void ensureToStringReturnsYearAsString() {

        final var regYear = new RegistrationYear(2015);

        assertEquals("2015", regYear.toString());
    }
}