package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AisafePasswordPolicyTest {

    private final AisafePasswordPolicy policy = new AisafePasswordPolicy();

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureValidPasswordIsAccepted() {

        final var password = "Abc123";

        assertTrue(policy.isSatisfiedBy(password));
    }

    @Test
    void ensurePasswordWithMoreComplexValidFormatIsAccepted() {

        final var password = "StrongPass1";

        assertTrue(policy.isSatisfiedBy(password));
    }

    // =========================================================
    // FAILURE CASES - NULL / EMPTY
    // =========================================================

    @Test
    void ensureNullPasswordIsRejected() {

        assertFalse(policy.isSatisfiedBy(null));
    }

    @Test
    void ensureEmptyPasswordIsRejected() {

        assertFalse(policy.isSatisfiedBy(""));
    }

    @Test
    void ensureBlankPasswordIsRejected() {

        assertFalse(policy.isSatisfiedBy("   "));
    }

    // =========================================================
    // FAILURE CASES - LENGTH RULE
    // =========================================================

    @Test
    void ensurePasswordTooShortIsRejected() {

        final var password = "A1b"; // < 6 chars

        assertFalse(policy.isSatisfiedBy(password));
    }

    // =========================================================
    // FAILURE CASES - DIGIT RULE
    // =========================================================

    @Test
    void ensurePasswordWithoutDigitIsRejected() {

        final var password = "Abcdefg";

        assertFalse(policy.isSatisfiedBy(password));
    }

    // =========================================================
    // FAILURE CASES - UPPERCASE RULE
    // =========================================================

    @Test
    void ensurePasswordWithoutUppercaseIsRejected() {

        final var password = "abcdef1";

        assertFalse(policy.isSatisfiedBy(password));
    }

    // =========================================================
    // EDGE CASES
    // =========================================================

    @Test
    void ensurePasswordExactlySixCharactersValidIsAccepted() {

        final var password = "Abc12D";

        assertTrue(policy.isSatisfiedBy(password));
    }

    @Test
    void ensurePasswordMissingOnlyOneRuleFails() {

        // no uppercase
        assertFalse(policy.isSatisfiedBy("abcdef1"));

        // no digit
        assertFalse(policy.isSatisfiedBy("Abcdefg"));
    }
}