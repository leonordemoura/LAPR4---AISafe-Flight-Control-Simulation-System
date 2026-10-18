package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ObservationDate implements ValueObject {
    @Column(name = "observation_start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "observation_end_date", nullable = false)
    private LocalDateTime endDate;

    protected ObservationDate() {
        // For ORM.
    }

    public ObservationDate(final LocalDateTime startDate, final LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("ObservationDate start/end date cannot be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("ObservationDate end date must be after start date");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDateTime startDate() {
        return startDate;
    }

    public LocalDateTime endDate() {
        return endDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ObservationDate that)) return false;
        return startDate.equals(that.startDate) && endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }
}
