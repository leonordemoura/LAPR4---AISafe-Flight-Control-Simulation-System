package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReportFilePath implements ValueObject, Serializable {

    @Column(name = "file_path")
    private String value;

    protected ReportFilePath() {
        // for ORM
    }

    public ReportFilePath(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Report file path is required.");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportFilePath that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
