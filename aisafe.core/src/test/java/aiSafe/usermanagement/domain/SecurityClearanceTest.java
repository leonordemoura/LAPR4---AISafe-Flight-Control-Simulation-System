package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SecurityClearanceTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureSecurityClearanceIsCreatedSuccessfully() {

        final var clearance = new SecurityClearance(LocalDate.now().plusDays(10));

        assertNotNull(clearance);
    }

    @Test
    void ensureExpiryDateIsReturnedCorrectly() {

        final var date = LocalDate.of(2030, 1, 1);
        final var clearance = new SecurityClearance(date);

        assertEquals(date, clearance.expiryDate());
    }

    @Test
    void ensureIsValidOnReturnsTrueWhenDateIsBeforeExpiry() {

        final var clearance = new SecurityClearance(LocalDate.now().plusDays(10));

        assertTrue(clearance.isValidOn(LocalDate.now()));
    }

    @Test
    void ensureIsValidOnReturnsTrueOnExpiryDate() {

        final var date = LocalDate.now();
        final var clearance = new SecurityClearance(date);

        assertTrue(clearance.isValidOn(date));
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureExpiryDateCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new SecurityClearance(null);
        });
    }

    @Test
    void ensureIsValidOnCannotAcceptNullDate() {

        final var clearance = new SecurityClearance(LocalDate.now().plusDays(10));

        assertThrows(IllegalArgumentException.class, () -> {
            clearance.isValidOn(null);
        });
    }

    // =========================================================
    // INVALID LOGIC CASES
    // =========================================================

    @Test
    void ensureIsValidOnReturnsFalseWhenExpired() {

        final var clearance = new SecurityClearance(LocalDate.now().minusDays(1));

        assertFalse(clearance.isValidOn(LocalDate.now()));
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameExpiryDatesAreEqual() {

        final var date = LocalDate.of(2030, 1, 1);

        final var c1 = new SecurityClearance(date);
        final var c2 = new SecurityClearance(date);

        assertEquals(c1, c2);
    }

    @Test
    void ensureDifferentExpiryDatesAreNotEqual() {

        final var c1 = new SecurityClearance(LocalDate.of(2030, 1, 1));
        final var c2 = new SecurityClearance(LocalDate.of(2031, 1, 1));

        assertNotEquals(c1, c2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValue() {

        final var date = LocalDate.of(2030, 1, 1);

        final var c1 = new SecurityClearance(date);
        final var c2 = new SecurityClearance(date);

        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void ensureSecurityClearanceIsNotEqualToDifferentObjectType() {

        final var clearance = new SecurityClearance(LocalDate.now().plusDays(10));

        assertNotEquals(clearance, "not a security clearance");
    }

    @Test
    void ensureSecurityClearanceIsEqualToItself() {

        final var clearance = new SecurityClearance(LocalDate.now().plusDays(10));

        assertEquals(clearance, clearance);
    }
}