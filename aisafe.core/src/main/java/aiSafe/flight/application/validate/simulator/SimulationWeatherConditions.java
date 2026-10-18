package aiSafe.flight.application.validate.simulator;

import aiSafe.weatherdata.domain.Pressure;
import aiSafe.weatherdata.domain.Temperature;
import aiSafe.weatherdata.domain.WeatherData;
import aiSafe.weatherdata.domain.WindSpeed;

import java.util.Objects;

/**
 * Surface weather observation used to adjust ISA atmosphere and cruise wind during US085 fuel simulation.
 */
public record SimulationWeatherConditions(
        double seaLevelTemperatureKelvin,
        double seaLevelPressurePascals,
        double windDirectionDeg,
        double windSpeedMps
) {

    private static final double KNOTS_TO_METERS_PER_SECOND = 0.514444;

    public SimulationWeatherConditions {
        if (seaLevelTemperatureKelvin <= 0) {
            throw new IllegalArgumentException("Sea level temperature must be positive in Kelvin.");
        }
        if (seaLevelPressurePascals <= 0) {
            throw new IllegalArgumentException("Sea level pressure must be positive.");
        }
        if (windDirectionDeg < 0 || windDirectionDeg >= 360) {
            throw new IllegalArgumentException("Wind direction must be in [0, 360).");
        }
        if (windSpeedMps < 0) {
            throw new IllegalArgumentException("Wind speed cannot be negative.");
        }
    }

    public static SimulationWeatherConditions from(final WeatherData weather) {
        Objects.requireNonNull(weather, "Weather data is required.");
        return new SimulationWeatherConditions(
                toKelvin(weather.temperature()),
                toPascals(weather.pressure()),
                weather.windDirection().value(),
                toMetersPerSecond(weather.windSpeed()));
    }

    private static double toKelvin(final Temperature temperature) {
        return switch (temperature.unit()) {
            case C -> temperature.value() + 273.15;
            case F -> (temperature.value() - 32.0) * 5.0 / 9.0 + 273.15;
        };
    }

    private static double toPascals(final Pressure pressure) {
        return switch (pressure.unit()) {
            case HPA -> pressure.value() * 100.0;
            case PA -> pressure.value();
        };
    }

    private static double toMetersPerSecond(final WindSpeed windSpeed) {
        return switch (windSpeed.unit()) {
            case MPS -> windSpeed.value();
            case KMH -> windSpeed.value() / 3.6;
            case KT -> windSpeed.value() * KNOTS_TO_METERS_PER_SECOND;
        };
    }
}
