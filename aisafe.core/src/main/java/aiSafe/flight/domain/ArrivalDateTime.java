package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ArrivalDateTime implements ValueObject, Serializable {

    @Column(name = "arrival_date_time", nullable = false)
    private LocalDateTime value;

    protected ArrivalDateTime() {
        // for ORM
    }

    public ArrivalDateTime(final LocalDateTime arrival, final LocalDateTime departure) {
        this(arrival, departure, true);
    }

    /**
     * US081 – scheduled arrival from an imported DSL file (no “must be in the future” rule).
     */
    public static ArrivalDateTime fromDslImport(final LocalDateTime arrival, final LocalDateTime departure) {
        return new ArrivalDateTime(arrival, departure, false);
    }

    private ArrivalDateTime(final LocalDateTime arrival, final LocalDateTime departure, final boolean rejectPast) {
        if (arrival == null) {
            throw new IllegalArgumentException("Arrival date/time is required.");
        }
        if (departure == null) {
            throw new IllegalArgumentException("Departure date/time is required to validate arrival.");
        }
        if (!arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Arrival date/time must be after departure.");
        }
        if (rejectPast && arrival.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Arrival date/time cannot be in the past (now is " + LocalDateTime.now() + ").");
        }
        this.value = arrival;
    }

    public LocalDateTime value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrivalDateTime that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
