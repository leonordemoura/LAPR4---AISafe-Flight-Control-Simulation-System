package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FuelTypeName implements Serializable, ValueObject {
    @Column (nullable = false, unique = true)
    private String type;

    protected FuelTypeName() {
        // for ORM
    }

    public FuelTypeName(final String type) {
        Preconditions.nonEmpty(type, "Fuel type name cannot be null or empty.");
        this.type = type;
    }

    public static FuelTypeName valueOf(final String type) {
        return new FuelTypeName(type);
    }

    public String type() {
        return this.type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FuelTypeName that = (FuelTypeName) o;
        return type.equalsIgnoreCase(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
