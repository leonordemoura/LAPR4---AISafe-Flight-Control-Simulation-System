package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailDomainTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureEmailDomainIsCreatedSuccessfully() {

        final var domain = new EmailDomain("company.com");

        assertNotNull(domain);
    }

    @Test
    void ensureValueReturnsCorrectDomain() {

        final var domain = new EmailDomain("company.com");

        assertEquals("company.com", domain.value());
    }

    @Test
    void ensureToStringReturnsDomainValue() {

        final var domain = new EmailDomain("company.com");

        assertEquals("company.com", domain.toString());
    }

    // =========================================================
    // NORMALIZATION TESTS
    // =========================================================

    @Test
    void ensureDomainIsTrimmedAndLowercased() {

        final var domain = new EmailDomain("   Company.COM   ");

        assertEquals("company.com", domain.value());
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureEmailDomainCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new EmailDomain(null);
        });
    }

    @Test
    void ensureEmailDomainCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new EmailDomain("");
        });
    }

    @Test
    void ensureEmailDomainCannotBeBlank() {

        assertThrows(IllegalArgumentException.class, () -> {
            new EmailDomain("   ");
        });
    }

    // =========================================================
    // BUSINESS RULE TESTS (AUTHORIZED DOMAINS)
    // =========================================================

    @Test
    void ensureAuthorizedDomainIsAccepted() {

        assertDoesNotThrow(() -> {
            new EmailDomain("aisafe.local");
        });
    }

    @Test
    void ensureAnotherAuthorizedDomainIsAccepted() {

        assertDoesNotThrow(() -> {
            new EmailDomain("company.com");
        });
    }

    @Test
    void ensureUnauthorizedDomainIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new EmailDomain("gmail.com");
        });
    }

    @Test
    void ensureUnauthorizedDomainWithSpacesIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {
            new EmailDomain("  gmail.com  ");
        });
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameDomainsAreEqual() {

        final var d1 = new EmailDomain("company.com");
        final var d2 = new EmailDomain("company.com");

        assertEquals(d1, d2);
    }

    @Test
    void ensureDomainsAreEqualIgnoringCase() {

        final var d1 = new EmailDomain("Company.com");
        final var d2 = new EmailDomain("company.com");

        assertEquals(d1, d2);
    }

    @Test
    void ensureDifferentDomainsAreNotEqual() {

        final var d1 = new EmailDomain("company.com");
        final var d2 = new EmailDomain("aisafe.local");

        assertNotEquals(d1, d2);
    }

    @Test
    void ensureHashCodeIsEqualForSameDomain() {

        final var d1 = new EmailDomain("company.com");
        final var d2 = new EmailDomain("company.com");

        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void ensureEmailDomainIsNotEqualToDifferentObjectType() {

        final var domain = new EmailDomain("company.com");

        assertNotEquals(domain, "company.com");
    }

    @Test
    void ensureEmailDomainIsEqualToItself() {

        final var domain = new EmailDomain("company.com");

        assertEquals(domain, domain);
    }

    // =========================================================
    // STATIC BEHAVIOR TESTS
    // =========================================================

    @Test
    void ensureAuthorizedDomainsListIsNotEmpty() {

        assertFalse(EmailDomain.authorizedDomains().isEmpty());
    }

    @Test
    void ensureAuthorizedDomainsContainsCompanyCom() {

        assertTrue(EmailDomain.authorizedDomains().contains("company.com"));
    }

    @Test
    void ensureAuthorizedDomainsContainsAisafeLocal() {

        assertTrue(EmailDomain.authorizedDomains().contains("aisafe.local"));
    }
}