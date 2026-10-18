package aiSafe.aircontrolarea.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import eapli.framework.domain.model.ValueObject;
import java.util.Objects;

@Embeddable
public class AreaName implements ValueObject {
    @Column(name = "area_name", nullable = false, unique = true)
    private String value;

    protected AreaName() {
        // For ORM.
    }

    public AreaName(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("AreaName cannot be null or blank");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AreaName areaName)) return false;
        return value.equals(areaName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
