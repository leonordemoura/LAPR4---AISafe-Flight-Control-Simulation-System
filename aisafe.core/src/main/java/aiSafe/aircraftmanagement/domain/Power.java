package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;

@Embeddable
public class Power implements ValueObject, Serializable {
    @Column (nullable = false)
    private Integer value;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private Unit unit;

    protected Power() {
        // for ORM
    }

    public Power(final Integer value, final Unit unit) {
        Preconditions.noneNull(value, "Power value cannot be null.");
        Preconditions.nonNegative(value, "Power value must be a non-negative number.");
        Preconditions.noneNull(unit, "Power unit cannot be null or empty.");
        this.value = value;
        this.unit = unit;
    }

    public static Power valueOf(final Integer value, final Unit unit) {
        return new Power(value, unit);
    }

    public Integer value() {
        return this.value;
    }

    public Unit unit() {
        return this.unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Power that = (Power) o;
        return value.equals(that.value) && unit.equals(that.unit);
    }

    @Override
    public int hashCode() {
        return value.hashCode() * 31 + unit.hashCode();
    }
}
