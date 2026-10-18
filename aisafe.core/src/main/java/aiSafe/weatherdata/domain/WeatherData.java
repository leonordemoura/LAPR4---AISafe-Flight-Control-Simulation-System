package aiSafe.weatherdata.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(name = "weather_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"area_code", "observation_start_date", "observation_end_date", "weather_source",
                "subarea_min_lat", "subarea_max_lat", "subarea_min_lon", "subarea_max_lon", "subarea_min_alt", "subarea_max_alt"})
})
public class WeatherData implements AggregateRoot<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "area_code", nullable = false))
    private AreaCode areaCode;

    @Embedded
    private ObservationDate observationDate;

    @Embedded
    private WeatherSource weatherSource;

    @Embedded
    private WeatherSubArea weatherSubArea;

    @Embedded
    private WindDirection windDirection;

    @Embedded
    private WindSpeed windSpeed;

    @Embedded
    private Temperature temperature;

    @Embedded
    private Pressure pressure;

    protected WeatherData() {
        // For ORM.
    }

    public WeatherData(final AreaCode areaCode, final ObservationDate observationDate, final WeatherSource weatherSource,
                       final WeatherSubArea weatherSubArea,
                       final WindDirection windDirection,
                       final WindSpeed windSpeed, final Temperature temperature, final Pressure pressure) {
        if (areaCode == null || observationDate == null || weatherSource == null
                || weatherSubArea == null || windDirection == null || windSpeed == null || temperature == null || pressure == null) {
            throw new IllegalArgumentException("WeatherData mandatory fields must not be null");
        }
        this.areaCode = areaCode;
        this.observationDate = observationDate;
        this.weatherSource = weatherSource;
        this.weatherSubArea = weatherSubArea;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.temperature = temperature;
        this.pressure = pressure;
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof WeatherData that)) {
            return false;
        }
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.areaCode, that.areaCode)
                && Objects.equals(this.observationDate, that.observationDate)
                && Objects.equals(this.weatherSource, that.weatherSource)
                && Objects.equals(this.weatherSubArea, that.weatherSubArea)
                && Objects.equals(this.windDirection, that.windDirection)
                && Objects.equals(this.windSpeed, that.windSpeed)
                && Objects.equals(this.temperature, that.temperature)
                && Objects.equals(this.pressure, that.pressure);
    }

    public AreaCode areaCode() {
        return areaCode;
    }

    public ObservationDate observationDate() {
        return observationDate;
    }

    public WeatherSource weatherSource() {
        return weatherSource;
    }

    public WeatherSubArea weatherSubArea() {
        return weatherSubArea;
    }

    public WindDirection windDirection() {
        return windDirection;
    }

    public WindSpeed windSpeed() {
        return windSpeed;
    }

    public Temperature temperature() {
        return temperature;
    }

    public Pressure pressure() {
        return pressure;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherData that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
