package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public class ReportDate implements ValueObject, Serializable {

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    protected ReportDate() {
        // for ORM
    }

    public ReportDate(final LocalDateTime generatedAt) {
        if (generatedAt == null) {
            throw new IllegalArgumentException("Report date is required.");
        }
        this.generatedAt = generatedAt;
    }

    public LocalDateTime generatedAt() {
        return generatedAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportDate that)) return false;
        return Objects.equals(generatedAt, that.generatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generatedAt);
    }
}
