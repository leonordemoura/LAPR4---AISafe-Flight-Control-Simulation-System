package aiSafe.usermanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SkillsAssessmentTest {

    // =========================================================
    // SUCCESS CASES
    // =========================================================

    @Test
    void ensureSkillsAssessmentIsCreatedSuccessfully() {

        final var assessment = new SkillsAssessment(LocalDate.now());

        assertNotNull(assessment);
    }

    @Test
    void ensureLastAssessmentDateIsReturnedCorrectly() {

        final var date = LocalDate.of(2024, 1, 1);
        final var assessment = new SkillsAssessment(date);

        assertEquals(date, assessment.lastAssessmentDate());
    }

    @Test
    void ensureIsValidOnReturnsTrueWithinFiveYears() {

        final var assessmentDate = LocalDate.now().minusYears(3);
        final var assessment = new SkillsAssessment(assessmentDate);

        assertTrue(assessment.isValidOn(LocalDate.now()));
    }

    @Test
    void ensureIsValidOnReturnsTrueExactlyAtBoundary() {

        final var assessmentDate = LocalDate.now().minusYears(4).minusMonths(11);
        final var assessment = new SkillsAssessment(assessmentDate);

        assertTrue(assessment.isValidOn(LocalDate.now()));
    }

    // =========================================================
    // VALIDATION TESTS
    // =========================================================

    @Test
    void ensureLastAssessmentDateCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new SkillsAssessment(null);
        });
    }

    @Test
    void ensureIsValidOnCannotAcceptNullDate() {

        final var assessment = new SkillsAssessment(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> {
            assessment.isValidOn(null);
        });
    }

    // =========================================================
    // BUSINESS RULE TESTS
    // =========================================================

    @Test
    void ensureIsValidOnReturnsFalseAfterFiveYears() {

        final var assessmentDate = LocalDate.now().minusYears(6);
        final var assessment = new SkillsAssessment(assessmentDate);

        assertFalse(assessment.isValidOn(LocalDate.now()));
    }

    @Test
    void ensureIsValidOnReturnsFalseExactlyAtFiveYearsBoundary() {

        final var assessmentDate = LocalDate.now().minusYears(5);
        final var assessment = new SkillsAssessment(assessmentDate);

        assertFalse(assessment.isValidOn(LocalDate.now()));
    }

    // =========================================================
    // EQUALITY TESTS
    // =========================================================

    @Test
    void ensureSameLastAssessmentDatesAreEqual() {

        final var date = LocalDate.of(2024, 1, 1);

        final var s1 = new SkillsAssessment(date);
        final var s2 = new SkillsAssessment(date);

        assertEquals(s1, s2);
    }

    @Test
    void ensureDifferentLastAssessmentDatesAreNotEqual() {

        final var s1 = new SkillsAssessment(LocalDate.of(2024, 1, 1));
        final var s2 = new SkillsAssessment(LocalDate.of(2023, 1, 1));

        assertNotEquals(s1, s2);
    }

    @Test
    void ensureHashCodeIsEqualForSameValue() {

        final var date = LocalDate.of(2024, 1, 1);

        final var s1 = new SkillsAssessment(date);
        final var s2 = new SkillsAssessment(date);

        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void ensureSkillsAssessmentIsNotEqualToDifferentObjectType() {

        final var assessment = new SkillsAssessment(LocalDate.now());

        assertNotEquals(assessment, "not a skills assessment");
    }

    @Test
    void ensureSkillsAssessmentIsEqualToItself() {

        final var assessment = new SkillsAssessment(LocalDate.now());

        assertEquals(assessment, assessment);
    }
}