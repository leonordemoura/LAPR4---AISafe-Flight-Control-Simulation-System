package aiSafe.usermanagement.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embedded;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;

@Embeddable
public class AlsafeProfile implements Serializable {
    @Embedded
    private SecurityClearance securityClearance;

    @Embedded
    private SkillsAssessment skillsAssessment;

    protected AlsafeProfile() {
        // For ORM
    }

    public AlsafeProfile(final SecurityClearance securityClearance, final SkillsAssessment skillsAssessment) {
        Preconditions.nonNull(securityClearance, "securityClearance cannot be null");
        Preconditions.nonNull(skillsAssessment, "skillsAssessment cannot be null");
        this.securityClearance = securityClearance;
        this.skillsAssessment = skillsAssessment;
        ensureSecurityReadiness();
    }

    public SecurityClearance securityClearance() {
        return securityClearance;
    }

    public SkillsAssessment skillsAssessment() {
        return skillsAssessment;
    }

    public void ensureSecurityReadiness() {

        final LocalDate today = LocalDate.now();

        Preconditions.ensure(
                securityClearance.isValidOn(today),
                "Security clearance is expired"
        );

        Preconditions.ensure(
                skillsAssessment.isValidOn(today),
                "Skills assessment must be renewed every 5 years"
        );
    }
}

