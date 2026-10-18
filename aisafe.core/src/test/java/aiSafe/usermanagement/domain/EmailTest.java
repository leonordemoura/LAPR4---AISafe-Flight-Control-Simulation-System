package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureEmailIsCreatedSuccessfully() {

        final var email = new Email("john.doe@company.com");

        assertNotNull(email);
    }

    @Test
    void ensureValueReturnsNormalizedEmail() {

        final var email = new Email("John.Doe@Company.COM");

        assertEquals("john.doe@company.com", email.value());
    }

    @Test
    void ensureToStringReturnsEmailValue() {

        final var email = new Email("john.doe@company.com");

        assertEquals("john.doe@company.com", email.toString());
    }

    @Test
    void ensureDomainIsExtractedCorrectly() {

        final var email = new Email("john.doe@company.com");

        assertEquals("company.com", email.domain().value());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureEmailCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email(null);
        });
    }

    @Test
    void ensureEmailCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email("");
        });
    }

    @Test
    void ensureEmailCannotBeBlank() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email("   ");
        });
    }

    @Test
    void ensureInvalidEmailFormatIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email("invalid-email");
        });
    }

    @Test
    void ensureEmailWithoutAtIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email("john.doe.company.com");
        });
    }

    @Test
    void ensureEmailWithoutDomainIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new Email("john.doe@");
        });
    }

    // =========================================================
    // NORMALIZATION TESTS
    // =========================================================

    @Test
    void ensureEmailIsTrimmedAndLowercased() {

        final var email = new Email("   John.Doe@Company.COM   ");

        assertEquals("john.doe@company.com", email.value());
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameEmailsAreEqual() {

        final var email1 = new Email("john.doe@company.com");
        final var email2 = new Email("john.doe@company.com");

        assertEquals(email1, email2);
    }

    @Test
    void ensureEmailsAreEqualIgnoringCaseAndSpaces() {

        final var email1 = new Email("John.Doe@Company.com");
        final var email2 = new Email("john.doe@company.com");

        assertEquals(email1, email2);
    }

    @Test
    void ensureDifferentEmailsAreNotEqual() {

        final var email1 = new Email("john1@company.com");
        final var email2 = new Email("john2@company.com");

        assertNotEquals(email1, email2);
    }

    @Test
    void ensureHashCodeIsEqualForSameEmail() {

        final var email1 = new Email("john.doe@company.com");
        final var email2 = new Email("john.doe@company.com");

        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void ensureEmailIsNotEqualToDifferentObjectType() {

        final var email = new Email("john.doe@company.com");

        assertNotEquals(email, "john.doe@company.com");
    }

    @Test
    void ensureEmailIsEqualToItself() {

        final var email = new Email("john.doe@company.com");

        assertEquals(email, email);
    }
}