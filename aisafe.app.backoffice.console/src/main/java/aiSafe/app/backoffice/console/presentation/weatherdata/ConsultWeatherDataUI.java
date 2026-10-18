package aiSafe.app.backoffice.console.presentation.weatherdata;

import aiSafe.weatherdata.application.ConsultWeatherDataController;
import aiSafe.weatherdata.application.ConsultWeatherDataDTO;
import aiSafe.weatherdata.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class ConsultWeatherDataUI extends AbstractUI {

    private final ConsultWeatherDataController controller = new ConsultWeatherDataController();

    @Override
    protected boolean doShow() {
        try {
            final String areaCode = Console.readNonEmptyLine("Area code:", "Area code is mandatory.");
            final LocalDate date = readDate("Date (e.g. 2026-04-19):");

            final List<ConsultWeatherDataDTO> results = controller.consultWeatherData(areaCode, date);

            if (results.isEmpty()) {
                System.out.println("No weather data found for area '" + areaCode + "' on " + date + ".");
                return false;
            }

            System.out.println();
            System.out.println("Found " + results.size() + " record(s):");
            System.out.println("=".repeat(70));

            for (int i = 0; i < results.size(); i++) {
                printWeatherData(i + 1, results.get(i));
            }

        } catch (final IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Consult Weather Data";
    }

    private LocalDate readDate(final String prompt) {
        while (true) {
            final String raw = Console.readNonEmptyLine(prompt, "Date is mandatory.");
            try {
                return LocalDate.parse(raw.trim());
            } catch (final DateTimeParseException ex) {
                System.out.println("Invalid date format. Use ISO format like 2026-04-19.");
            }
        }
    }

    private static void printWeatherData(final int index, final ConsultWeatherDataDTO dto) {
        System.out.println("Record #" + index);
        System.out.printf(Locale.US, "  Period    : %s  →  %s%n",
                dto.observationStartDate, dto.observationEndDate);
        System.out.printf(Locale.US, "  Sub-area  : %s%n", dto.weatherSubArea);
        System.out.printf(Locale.US, "  Wind      : %.1f°  at %.2f%n",
                dto.windDirection, dto.windSpeed);
        System.out.printf(Locale.US, "  Temp      : %.2f%n", dto.temperature);
        System.out.printf(Locale.US, "  Pressure  : %.2f%n", dto.pressure);
        System.out.println("-".repeat(70));
    }
}