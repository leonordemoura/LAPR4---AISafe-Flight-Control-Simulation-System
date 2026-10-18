package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Embeddable
public class SkillsAssessment implements ValueObject, Serializable {
    private static final long RENEWAL_YEARS = 5;

    @Column(name = "skills_assessment_last_date")
    private LocalDate lastAssessmentDate;

    protected SkillsAssessment() {
        // For ORM
    }

    public SkillsAssessment(final LocalDate lastAssessmentDate) {
        Preconditions.noneNull(lastAssessmentDate, "Last assessment date must be non-null");
        this.lastAssessmentDate = lastAssessmentDate;
    }

    public LocalDate lastAssessmentDate() {
        return lastAssessmentDate;
    }

    public boolean isValidOn(final LocalDate date) {
        Preconditions.noneNull(date, "Date must be non-null");
        final long years = ChronoUnit.YEARS.between(lastAssessmentDate, date);
        return years < RENEWAL_YEARS;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillsAssessment that)) return false;
        return Objects.equals(lastAssessmentDate, that.lastAssessmentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastAssessmentDate);
    }
}

