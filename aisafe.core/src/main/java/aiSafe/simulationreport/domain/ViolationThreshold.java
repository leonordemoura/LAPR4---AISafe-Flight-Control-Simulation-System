package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ViolationThreshold implements ValueObject, Serializable {

    @Column(name = "violation_threshold", nullable = false)
    private int value;

    protected ViolationThreshold() {
        // for ORM
    }

    public ViolationThreshold(final int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Violation threshold cannot be negative.");
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ViolationThreshold that)) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
