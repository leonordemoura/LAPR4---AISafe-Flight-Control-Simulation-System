package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class DepartureDateTime implements ValueObject, Serializable {

    @Column(name = "departure_date_time", nullable = false)
    private LocalDateTime value;

    protected DepartureDateTime() {
        // for ORM
    }

    public DepartureDateTime(final LocalDateTime value) {
        this(value, true);
    }

    /**
     * US081 – scheduled departure from an imported DSL file (no “must be in the future” rule).
     */
    public static DepartureDateTime fromDslImport(final LocalDateTime value) {
        return new DepartureDateTime(value, false);
    }

    private DepartureDateTime(final LocalDateTime value, final boolean rejectPast) {
        if (value == null) {
            throw new IllegalArgumentException("Departure date/time is required.");
        }
        if (rejectPast && value.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Departure date/time cannot be in the past (now is " + LocalDateTime.now() + ").");
        }
        this.value = value;
    }

    public LocalDateTime value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof DepartureDateTime that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
