package aiSafe.weatherdata.importer;

import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WindSpeedUnit;

import java.time.LocalDateTime;
import java.util.Objects;

public class WeatherDataImportRow {

    private final String areaCode;
    private final LocalDateTime observationStart;
    private final LocalDateTime observationEnd;
    private final double minLatitude;
    private final double maxLatitude;
    private final double minLongitude;
    private final double maxLongitude;
    private final double minAltitude;
    private final double maxAltitude;
    private final String weatherSource;
    private final double windDirection;
    private final double windSpeed;
    private final WindSpeedUnit windSpeedUnit;
    private final double temperature;
    private final TemperatureUnit temperatureUnit;
    private final double pressure;
    private final PressureUnit pressureUnit;

    public WeatherDataImportRow(final String areaCode,
                                final LocalDateTime observationStart,
                                final LocalDateTime observationEnd,
                                final double minLatitude, final double maxLatitude,
                                final double minLongitude, final double maxLongitude,
                                final double minAltitude, final double maxAltitude,
                                final String weatherSource,
                                final double windDirection,
                                final double windSpeed, final WindSpeedUnit windSpeedUnit,
                                final double temperature, final TemperatureUnit temperatureUnit,
                                final double pressure, final PressureUnit pressureUnit) {
        this.areaCode = areaCode;
        this.observationStart = observationStart;
        this.observationEnd = observationEnd;
        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.minAltitude = minAltitude;
        this.maxAltitude = maxAltitude;
        this.weatherSource = weatherSource;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.windSpeedUnit = windSpeedUnit;
        this.temperature = temperature;
        this.temperatureUnit = temperatureUnit;
        this.pressure = pressure;
        this.pressureUnit = pressureUnit;
    }

    public String areaCode() { return areaCode; }
    public LocalDateTime observationStart() { return observationStart; }
    public LocalDateTime observationEnd() { return observationEnd; }
    public double minLatitude() { return minLatitude; }
    public double maxLatitude() { return maxLatitude; }
    public double minLongitude() { return minLongitude; }
    public double maxLongitude() { return maxLongitude; }
    public double minAltitude() { return minAltitude; }
    public double maxAltitude() { return maxAltitude; }
    public String weatherSource() { return weatherSource; }
    public double windDirection() { return windDirection; }
    public double windSpeed() { return windSpeed; }
    public WindSpeedUnit windSpeedUnit() { return windSpeedUnit; }
    public double temperature() { return temperature; }
    public TemperatureUnit temperatureUnit() { return temperatureUnit; }
    public double pressure() { return pressure; }
    public PressureUnit pressureUnit() { return pressureUnit; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherDataImportRow that)) return false;
        return Double.compare(that.minLatitude, minLatitude) == 0
                && Double.compare(that.maxLatitude, maxLatitude) == 0
                && Double.compare(that.minLongitude, minLongitude) == 0
                && Double.compare(that.maxLongitude, maxLongitude) == 0
                && Double.compare(that.minAltitude, minAltitude) == 0
                && Double.compare(that.maxAltitude, maxAltitude) == 0
                && Double.compare(that.windDirection, windDirection) == 0
                && Double.compare(that.windSpeed, windSpeed) == 0
                && Double.compare(that.temperature, temperature) == 0
                && Double.compare(that.pressure, pressure) == 0
                && Objects.equals(areaCode, that.areaCode)
                && Objects.equals(observationStart, that.observationStart)
                && Objects.equals(observationEnd, that.observationEnd)
                && Objects.equals(weatherSource, that.weatherSource)
                && windSpeedUnit == that.windSpeedUnit
                && temperatureUnit == that.temperatureUnit
                && pressureUnit == that.pressureUnit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaCode, observationStart, observationEnd,
                minLatitude, maxLatitude, minLongitude, maxLongitude,
                minAltitude, maxAltitude, weatherSource, windDirection,
                windSpeed, windSpeedUnit, temperature, temperatureUnit,
                pressure, pressureUnit);
    }
}