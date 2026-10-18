package aiSafe.weatherdata.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class WeatherSource implements ValueObject {
    @Column(name = "weather_source", nullable = false)
    private String value;

    protected WeatherSource() {
        // For ORM.
    }

    public WeatherSource(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("WeatherSource cannot be null or blank");
        }
        this.value = value.trim();
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherSource that)) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
