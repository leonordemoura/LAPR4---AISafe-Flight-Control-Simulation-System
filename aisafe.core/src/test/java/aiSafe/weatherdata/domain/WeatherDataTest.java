package aiSafe.weatherdata.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// WeatherDataTest
public class WeatherDataTest {

    @Test
    public void ensureWeatherDataCannotHaveNullAreaCode() {
        // Arrange
        AreaCode nullAreaCode = null;
        ObservationDate date = new ObservationDate(
                LocalDateTime.of(2026, 4, 19, 14, 0),
                LocalDateTime.of(2026, 4, 19, 15, 0));
        WeatherSource source = new WeatherSource("IPMA");
        WeatherSubArea subArea = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 2000);
        WindDirection direction = new WindDirection(180.0);
        WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherData(nullAreaCode, date, source, subArea,
                    direction, speed, temp, pressure);
        });
    }

    @Test
    public void ensureWeatherDataCannotHaveNullObservationDate() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        ObservationDate nullDate = null;
        WeatherSource source = new WeatherSource("IPMA");
        WeatherSubArea subArea = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 2000);
        WindDirection direction = new WindDirection(180.0);
        WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherData(areaCode, nullDate, source, subArea,
                    direction, speed, temp, pressure);
        });
    }

    @Test
    public void ensureWeatherDataCannotHaveNullWeatherSource() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        ObservationDate date = new ObservationDate(
                LocalDateTime.of(2026, 4, 19, 14, 0),
                LocalDateTime.of(2026, 4, 19, 15, 0));
        WeatherSource nullSource = null;
        WeatherSubArea subArea = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 2000);
        WindDirection direction = new WindDirection(180.0);
        WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new WeatherData(areaCode, date, nullSource, subArea,
                    direction, speed, temp, pressure);
        });
    }

    @Test
    public void ensureWeatherDataIsCreatedSuccessfully() {
        // Arrange
        AreaCode areaCode = new AreaCode("LPPT");
        ObservationDate date = new ObservationDate(
                LocalDateTime.of(2026, 4, 19, 14, 0),
                LocalDateTime.of(2026, 4, 19, 15, 0));
        WeatherSource source = new WeatherSource("IPMA");
        WeatherSubArea subArea = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 2000);
        WindDirection direction = new WindDirection(180.0);
        WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);

        // Act
        WeatherData weatherData = new WeatherData(areaCode,
                date, source, subArea, direction, speed, temp, pressure);

        // Assert
        assertEquals(areaCode, weatherData.areaCode());
        assertEquals(date, weatherData.observationDate());
        assertEquals(source, weatherData.weatherSource());
        assertEquals(subArea, weatherData.weatherSubArea());
        assertEquals(direction, weatherData.windDirection());
        assertEquals(speed, weatherData.windSpeed());
        assertEquals(temp, weatherData.temperature());
        assertEquals(pressure, weatherData.pressure());
    }

    @Test
    void ensureWeatherDataCannotHaveNullWeatherSubArea() {
        final AreaCode areaCode = new AreaCode("LPPT");
        final ObservationDate date = new ObservationDate(
                LocalDateTime.of(2026, 4, 19, 14, 0),
                LocalDateTime.of(2026, 4, 19, 15, 0));
        final WeatherSource source = new WeatherSource("IPMA");
        final WeatherSubArea nullSubArea = null;
        final WindDirection direction = new WindDirection(180.0);
        final WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        final Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        final Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);

        assertThrows(IllegalArgumentException.class, () ->
                new WeatherData(areaCode, date, source, nullSubArea, direction, speed, temp, pressure));
    }

    @Test
    void ensureSameAs_rejects_nonWeatherData() {
        final WeatherData data = baseWeatherDataBuilder();
        assertFalse(data.sameAs("other"));
    }

    @Test
    void ensureSameAs_matches_when_all_embedded_fields_are_equal_and_ids_null() {
        final WeatherData a = baseWeatherDataBuilder();
        final WeatherData b = baseWeatherDataBuilder();
        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAs_false_when_any_embedded_value_differs() {
        final WeatherData base = baseWeatherDataBuilder();
        final WeatherData otherPressure = new WeatherData(
                base.areaCode(),
                base.observationDate(),
                base.weatherSource(),
                base.weatherSubArea(),
                base.windDirection(),
                base.windSpeed(),
                base.temperature(),
                new Pressure(900.0, PressureUnit.HPA));
        assertFalse(base.sameAs(otherPressure));
    }

    @Test
    void ensureSameAs_false_when_area_code_differs() {
        final WeatherData base = baseWeatherDataBuilder();
        final WeatherData other = new WeatherData(
                new AreaCode("OTHR"),
                base.observationDate(),
                base.weatherSource(),
                base.weatherSubArea(),
                base.windDirection(),
                base.windSpeed(),
                base.temperature(),
                base.pressure());
        assertFalse(base.sameAs(other));
    }

    @Test
    void ensureEquals_distinguishes_unpersisted_instances() {
        final WeatherData a = baseWeatherDataBuilder();
        final WeatherData b = baseWeatherDataBuilder();
        assertNotEquals(a, b);
        assertFalse(a.equals("not-weather-data"));
    }

    @Test
    void ensureEquals_is_reflexive() {
        final WeatherData a = baseWeatherDataBuilder();
        assertEquals(a, a);
    }

    @Test
    void ensureIdentity_is_null_before_persistence() {
        assertNull(baseWeatherDataBuilder().identity());
    }

    @Test
    void orm_constructor_is_provided() {
        new WeatherData();
    }

    private static WeatherData baseWeatherDataBuilder() {
        final AreaCode areaCode = new AreaCode("LPPT");
        final ObservationDate date = new ObservationDate(
                LocalDateTime.of(2026, 4, 19, 14, 0),
                LocalDateTime.of(2026, 4, 19, 15, 0));
        final WeatherSource source = new WeatherSource("IPMA");
        final WeatherSubArea subArea = new WeatherSubArea(38.5, 39.0, -9.5, -9.0, 0, 2000);
        final WindDirection direction = new WindDirection(180.0);
        final WindSpeed speed = new WindSpeed(20.0, WindSpeedUnit.KMH);
        final Temperature temp = new Temperature(15.0, TemperatureUnit.C);
        final Pressure pressure = new Pressure(1013.0, PressureUnit.HPA);
        return new WeatherData(areaCode, date, source, subArea, direction, speed, temp, pressure);
    }
}
