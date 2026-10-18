package aiSafe.app.backoffice.console.presentation.weatherdata;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.weatherdata.application.RegisterWeatherDataController;
import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class RegisterWeatherDataUI extends AbstractUI {
    private final RegisterWeatherDataController controller = new RegisterWeatherDataController();

    @Override
    protected boolean doShow() {
        try {
            final String areaCode = Console.readNonEmptyLine("Area code:", "Area code is mandatory.");
            final LocalDateTime observationStartDate = readObservationDate("Observation start date/time (e.g. 2026-04-19T14:00):");
            final LocalDateTime observationEndDate = readObservationDate("Observation end date/time (e.g. 2026-04-19T15:00):");

            printControlAreaLimits(controller.controlAreaByAreaCode(areaCode));

            final double minLat = Console.readDouble("Sub-area min latitude:");
            final double maxLat = Console.readDouble("Sub-area max latitude:");
            final double minLon = Console.readDouble("Sub-area min longitude:");
            final double maxLon = Console.readDouble("Sub-area max longitude:");
            final double minAlt = Console.readDouble("Sub-area min altitude (m):");
            final double maxAlt = Console.readDouble("Sub-area max altitude (m):");
            controller.validateSubArea(areaCode, minLat, maxLat, minLon, maxLon, minAlt, maxAlt);

            final String weatherSource = Console.readNonEmptyLine("Weather source:", "Weather source is mandatory.");
            final double windDirection = Console.readDouble("Wind direction (degrees):");
            final WindSpeedUnit windSpeedUnit = readEnumChoice("Wind speed unit", WindSpeedUnit.class);
            final double windSpeed = Console.readDouble("Wind speed:");
            final TemperatureUnit temperatureUnit = readEnumChoice("Temperature unit", TemperatureUnit.class);
            final double temperature = Console.readDouble("Temperature:");
            final PressureUnit pressureUnit = readEnumChoice("Pressure unit", PressureUnit.class);
            final double pressure = Console.readDouble("Pressure:");

            controller.registerWeatherData(
                    areaCode,
                    observationStartDate,
                    observationEndDate,
                    minLat,
                    maxLat,
                    minLon,
                    maxLon,
                    minAlt,
                    maxAlt,
                    weatherSource,
                    windDirection,
                    windSpeed,
                    windSpeedUnit,
                    temperature,
                    temperatureUnit,
                    pressure,
                    pressureUnit
            );

            System.out.println("Weather data registered successfully.");
        } catch (final IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering weather data: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Register Weather Data";
    }

    private static void printControlAreaLimits(final AirControlArea controlArea) {
        System.out.println();
        System.out.println("Air control area bounds (sub-area must lie inside this box, altitude up to the max):");
        System.out.println("  " + controlArea.areaName().value() + " [" + controlArea.areaCode().value() + "]");
        System.out.printf(Locale.US, "  Latitude:  min %.6f  max %.6f%n",
                controlArea.geoBoundary().minLatitude(), controlArea.geoBoundary().maxLatitude());
        System.out.printf(Locale.US, "  Longitude: min %.6f  max %.6f%n",
                controlArea.geoBoundary().minLongitude(), controlArea.geoBoundary().maxLongitude());
        System.out.printf(Locale.US, "  Max altitude (m): %.2f%n", controlArea.maxAltitude().value());
        System.out.println();
    }

    private LocalDateTime readObservationDate(final String prompt) {
        while (true) {
            final String raw = Console.readNonEmptyLine(
                    prompt,
                    "Observation date/time is mandatory."
            );
            try {
                return LocalDateTime.parse(raw.trim());
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid date/time format. Use ISO format like 2026-04-19T14:00.");
            }
        }
    }

    private <T extends Enum<T>> T readEnumChoice(final String label, final Class<T> enumClass) {
        final T[] values = enumClass.getEnumConstants();

        System.out.println(label + " options:");
        for (int i = 0; i < values.length; i++) {
            System.out.println(" " + (i + 1) + ". " + values[i]);
        }

        while (true) {
            final String input = Console.readNonEmptyLine(label + " (1-" + values.length + "):", label + " is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= values.length) {
                    return values[choice - 1];
                }
            } catch (final NumberFormatException ignored) {
                // Continue prompting until a valid selection is given.
            }
            System.out.println("Invalid choice. Please enter a number between 1 and " + values.length + ".");
        }
    }
}
