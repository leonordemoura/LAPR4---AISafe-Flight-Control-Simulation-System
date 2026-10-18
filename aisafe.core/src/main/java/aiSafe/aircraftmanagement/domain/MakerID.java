package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class MakerID implements ValueObject, Serializable, Comparable<MakerID> {
    @Column(nullable = false, unique = true, updatable = false)
    private Long id;

    protected MakerID(){
        // for ORM
    }

    public MakerID(final Long id) {
        Preconditions.noneNull(id, "Maker ID cannot be null");
        Preconditions.nonNegative(id, "Maker ID cannot be negative");
        this.id = id;
    }

    public static MakerID valueOf(final Long id) {
        return new MakerID(id);
    }

    public Long id() {
        return this.id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MakerID that = (MakerID) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(final MakerID other) {
        return this.id.compareTo(other.id);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
