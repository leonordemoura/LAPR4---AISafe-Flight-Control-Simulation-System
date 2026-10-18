package aiSafe.airtransportcompany.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class CompanyName implements ValueObject, Serializable {
    @Column(name = "companyName", nullable = false, unique = true)
    private String name;

    protected CompanyName() {
        // for ORM
    }

    private CompanyName(final String name) {
        this.name = name;
    }

    public static CompanyName valueOf(final String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be null or empty.");
        }
        return new CompanyName(name);
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CompanyName that = (CompanyName) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
