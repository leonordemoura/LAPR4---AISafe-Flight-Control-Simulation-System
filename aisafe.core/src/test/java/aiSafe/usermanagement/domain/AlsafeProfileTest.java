package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AlsafeProfileTest {

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private SecurityClearance validClearance() {
        return new SecurityClearance(LocalDate.now().plusYears(1));
    }

    private SkillsAssessment validAssessment() {
        return new SkillsAssessment(LocalDate.now().minusYears(1));
    }

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureAlsafeProfileIsCreatedSuccessfully() {

        final var profile = new AlsafeProfile(
                validClearance(),
                validAssessment()
        );

        assertNotNull(profile);
    }

    @Test
    void ensureSecurityClearanceIsReturnedCorrectly() {

        final var clearance = validClearance();

        final var profile = new AlsafeProfile(
                clearance,
                validAssessment()
        );

        assertEquals(clearance, profile.securityClearance());
    }

    @Test
    void ensureSkillsAssessmentIsReturnedCorrectly() {

        final var assessment = validAssessment();

        final var profile = new AlsafeProfile(
                validClearance(),
                assessment
        );

        assertEquals(assessment, profile.skillsAssessment());
    }

    @Test
    void ensureSecurityReadinessSucceedsWithValidData() {

        final var profile = new AlsafeProfile(
                validClearance(),
                validAssessment()
        );

        assertDoesNotThrow(profile::ensureSecurityReadiness);
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureSecurityClearanceCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AlsafeProfile(
                    null,
                    validAssessment()
            );
        });
    }

    @Test
    void ensureSkillsAssessmentCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AlsafeProfile(
                    validClearance(),
                    null
            );
        });
    }

    // =========================================================
    // BUSINESS RULE TESTS
    // =========================================================

    @Test
    void ensureExpiredSecurityClearanceIsRejected() {

        final var expiredClearance =
                new SecurityClearance(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> {
            new AlsafeProfile(
                    expiredClearance,
                    validAssessment()
            );
        });
    }

    @Test
    void ensureExpiredSkillsAssessmentIsRejected() {

        final var expiredAssessment =
                new SkillsAssessment(LocalDate.now().minusYears(6));

        assertThrows(IllegalArgumentException.class, () -> {
            new AlsafeProfile(
                    validClearance(),
                    expiredAssessment
            );
        });
    }

    @Test
    void ensureEnsureSecurityReadinessThrowsWhenClearanceExpires() {

        final var expiredClearance =
                new SecurityClearance(LocalDate.now().minusDays(1));

        final var assessment =
                validAssessment();

        assertThrows(IllegalArgumentException.class, () -> {
            final var profile = new AlsafeProfile(
                    expiredClearance,
                    assessment
            );

            profile.ensureSecurityReadiness();
        });
    }

    @Test
    void ensureEnsureSecurityReadinessThrowsWhenAssessmentExpires() {

        final var clearance =
                validClearance();

        final var expiredAssessment =
                new SkillsAssessment(LocalDate.now().minusYears(6));

        assertThrows(IllegalArgumentException.class, () -> {
            final var profile = new AlsafeProfile(
                    clearance,
                    expiredAssessment
            );

            profile.ensureSecurityReadiness();
        });
    }
}