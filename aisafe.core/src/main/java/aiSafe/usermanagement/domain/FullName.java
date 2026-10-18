package aiSafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FullName implements ValueObject, Serializable {
    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    protected FullName() {
        // For ORM
    }

    public FullName(final String firstName, final String lastName) {
        Preconditions.noneNull(firstName, lastName, "firstName and lastName cannot be null");
        Preconditions.ensure(!firstName.trim().isEmpty(), "firstName cannot be empty");
        Preconditions.ensure(!lastName.trim().isEmpty(), "lastName cannot be empty");
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public String firstName() {
        return firstName;
    }

    public String lastName() {
        return lastName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FullName that)) return false;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}

