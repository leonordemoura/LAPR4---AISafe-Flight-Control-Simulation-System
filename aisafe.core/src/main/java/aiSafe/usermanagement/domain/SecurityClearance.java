package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class SecurityClearance implements ValueObject, Serializable {
    @Column(name = "security_clearance_expiry")
    private LocalDate expiryDate;

    protected SecurityClearance() {
        // For ORM
    }

    public SecurityClearance(final LocalDate expiryDate) {
        Preconditions.noneNull(expiryDate, "Expiry date must be non-null");
        this.expiryDate = expiryDate;
    }

    public LocalDate expiryDate() {
        return expiryDate;
    }

    public boolean isValidOn(final LocalDate date) {
        Preconditions.noneNull(date, "Date must be non-null");
        return !expiryDate.isBefore(date);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SecurityClearance that)) return false;
        return Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expiryDate);
    }
}

