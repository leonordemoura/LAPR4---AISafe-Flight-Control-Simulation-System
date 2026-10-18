package aiSafe.flight.application;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Scheduled window of a flight plan used for pilot conflict checks.
 */
public record FlightPlanSchedule(LocalDateTime departure, LocalDateTime arrival) {

    public FlightPlanSchedule {
        Objects.requireNonNull(departure, "Departure is required.");
        Objects.requireNonNull(arrival, "Arrival is required.");
        if (!arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Arrival must be after departure.");
        }
    }

    /**
     * Half-open interval overlap: [departure, arrival) — touching at boundary is allowed.
     */
    public boolean overlaps(final FlightPlanSchedule other) {
        Objects.requireNonNull(other);
        return departure.isBefore(other.arrival) && arrival.isAfter(other.departure);
    }
}
