package aiSafe.aircraftModel.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MaximumSeats implements ValueObject {

    private int seats;

    protected MaximumSeats() {
        // ORM
    }

    public MaximumSeats(final int value) {
        Preconditions.ensure(value > 0, "Maximum seats must be positive");
        Preconditions.noneNull(value, "Maximum seats must be non-null");
        this.seats = value;
    }

    public int seats() {
        return seats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaximumSeats)) return false;
        MaximumSeats that = (MaximumSeats) o;
        return seats == that.seats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seats);
    }
}