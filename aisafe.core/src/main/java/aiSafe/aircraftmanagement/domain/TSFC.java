package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TSFC implements ValueObject, Serializable {
    @Column(nullable = false)
    private Double value;

    protected TSFC() {
        // for ORM
    }

    public TSFC(final Double value) {
        Preconditions.noneNull(value, "TSFC value cannot be null.");
        Preconditions.ensure(value > 0, "TSFC value must be a non-negative number.");
        this.value = value;
    }

    public static TSFC valueOf(final Double value) {
        return new TSFC(value);
    }

    public Double value() {
        return this.value;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TSFC that = (TSFC) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
