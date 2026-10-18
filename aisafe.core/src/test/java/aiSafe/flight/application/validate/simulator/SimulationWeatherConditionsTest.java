package aiSafe.flight.application.validate.simulator;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.weatherdata.domain.ObservationDate;
import aiSafe.weatherdata.domain.Pressure;
import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.Temperature;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WeatherSource;
import aiSafe.weatherdata.domain.WeatherSubArea;
import aiSafe.weatherdata.domain.WindDirection;
import aiSafe.weatherdata.domain.WindSpeed;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulationWeatherConditionsTest {

    @Test
    void from_converts_weather_units() {
        final WeatherData weather = new WeatherData(
                new AreaCode("LPPT"),
                new ObservationDate(
                        LocalDateTime.of(2026, 7, 7, 10, 0),
                        LocalDateTime.of(2026, 7, 7, 11, 0)),
                new WeatherSource("METAR"),
                new WeatherSubArea(38.0, 39.0, -9.5, -8.5, 0, 2000),
                new WindDirection(270),
                new WindSpeed(20, WindSpeedUnit.KT),
                new Temperature(15, TemperatureUnit.C),
                new Pressure(1013, PressureUnit.HPA));

        final SimulationWeatherConditions conditions = SimulationWeatherConditions.from(weather);

        assertEquals(288.15, conditions.seaLevelTemperatureKelvin(), 0.01);
        assertEquals(101300, conditions.seaLevelPressurePascals(), 1.0);
        assertEquals(270, conditions.windDirectionDeg(), 0.01);
        assertEquals(20 * 0.514444, conditions.windSpeedMps(), 0.001);
    }
}
