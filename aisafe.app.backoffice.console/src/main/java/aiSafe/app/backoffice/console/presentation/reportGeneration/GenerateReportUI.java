package aiSafe.app.backoffice.console.presentation.reportGeneration;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.reportGeneration.application.GenerateReportController;
import aiSafe.reportGeneration.domain.ReportType;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class GenerateReportUI extends AbstractUI {
    /** The controller that handles the business logic for generating reports. */
    private final GenerateReportController controller;

    private static final String OUTPUT_DIRECTORY = "reports/";

    /** Constructs the UI and initializes its controller. */
    public GenerateReportUI() {
        this.controller = new GenerateReportController();
    }

    /**
     * Collects user input and generates the report.
     * @return {@code true} if the report generation process was initiated successfully, {@code false} otherwise. Catches and displays any runtime exceptions that occur during the process.
     */
    @Override
    protected boolean doShow() {
        YearMonth reportPeriod = readMonthYear("Select report period:");
        ReportType reportType = readEnumChoice("Select report type:", ReportType.class);

        // Check if report type is implemented (only Monthly Statistics is currently implemented)
        if (reportType != ReportType.MONTHLY_STATISTICS) {
            System.out.println("\nNote: The '" + reportType + "' is planned for future sprints and is not yet available.");
            Console.readLine("\nPress Enter to return to the menu...");
            return false;
        }

        String areaCode = readAreaCode("Select Air Control Area:");
        try {
            final String filePath = controller.generateReport(reportPeriod, reportType, areaCode, OUTPUT_DIRECTORY + "/" + reportType.toString().replace(" ", ""));
            System.out.println("Report generated successfully for " + reportPeriod + " in area " + areaCode + ".");
            System.out.println("Report saved to: " + filePath);
            return true;
        } catch (final RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Prompts the user to enter a month and year for the report period.
     *
     * <p>Keeps prompting until a valid month (1–12) and year are entered.</p>
     *
     * @param label context label shown to the user (e.g. {@code "Select report period:"})
     * @return a {@code YearMonth} representing the selected month and year
     */
    private YearMonth readMonthYear(final String label) {
        System.out.println(label);

        final int currentYear = YearMonth.now().getYear();
        int month = -1;
        int year = -1;

        while (month < 1 || month > 12) {
            final String input = Console.readNonEmptyLine("Month (1-12):", "Month is mandatory.");
            try {
                month = Integer.parseInt(input.trim());
                if (month < 1 || month > 12) {
                    System.out.println("Invalid month. Please enter a number between 1 and 12.");
                }
            } catch (final NumberFormatException ignored) {
                System.out.println("Invalid input. Please enter a numeric value.");
            }
        }

        while (year < 1900 || year > currentYear) {
            final String input = Console.readNonEmptyLine("Year (1900-" + currentYear + "):", "Year is mandatory.");
            try {
                year = Integer.parseInt(input.trim());
                if (year < 1900 || year > currentYear) {
                    System.out.println("Invalid year. Please enter a year between 1900 and " + currentYear + ".");
                }
            } catch (final NumberFormatException ignored) {
                System.out.println("Invalid input. Please enter a numeric value.");
            }
        }

        return YearMonth.of(year, month);
    }

    /**
     * Prompts the user to select a report type from the available options.
     * @param label context label shown to the user (e.g. {@code "Select report type:"})
     * @return the selected {@code ReportType} enum value
     */
    private <T extends Enum<T>> T readEnumChoice(String label, Class<T> enumClass) {
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
            } catch (final NumberFormatException ignored) {}

            System.out.println("Invalid choice. Please enter a number between 1 and " + values.length + ".");
        }
    }

    /**
     * Displays a numbered list of available air control areas and returns the area code of the one chosen by the user.
     *
     * <p>Keeps prompting until a valid numeric selection is made.</p>
     *
     * @param label context label shown to the user (e.g. {@code "Select Air Control Area::"})
     * @return the area code string of the selected air control area
     * @throws IllegalStateException if no air control areas are registered in the system
     */
    private String readAreaCode(final String label) {
        final List<AirControlArea> areas = new ArrayList<>();
        controller.getAirControlArea().forEach(areas::add);

        if (areas.isEmpty()) {
            throw new IllegalStateException("No air control areas available. Please run bootstrap or register air control areas first.");
        }

        System.out.println(label + " options:");
        for (int i = 0; i < areas.size(); i++) {
            final AirControlArea a = areas.get(i);
            System.out.printf("  %d. %s (%s)%n", i + 1, a.areaName(), a.areaCode());
        }

        while (true) {
            final String input = Console.readNonEmptyLine(
                    label + " (1-" + areas.size() + "):", label + " is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= areas.size()) {
                    return areas.get(choice - 1).areaCode().toString();
                }
            } catch (final NumberFormatException ignored) {
                // fall through to error message below
            }
            System.out.println("Invalid choice. Please enter a number between 1 and " + areas.size() + ".");
        }
    }

    /**
     * Provides the headline for this UI, which is displayed at the top of the console when this UI is active.
     * @return a string representing the headline of this UI.
     */
    @Override
    public String headline() {
        return "Generate Monthly Statistics Report";
    }
}
