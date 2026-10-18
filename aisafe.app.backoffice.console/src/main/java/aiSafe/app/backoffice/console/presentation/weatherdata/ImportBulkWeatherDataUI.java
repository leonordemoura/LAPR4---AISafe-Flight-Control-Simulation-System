package aiSafe.app.backoffice.console.presentation.weatherdata;

import aiSafe.weatherdata.application.ImportBulkWeatherDataController;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.csv.CsvWeatherDataImporter;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ImportBulkWeatherDataUI extends AbstractUI {

    private final ImportBulkWeatherDataController controller = new ImportBulkWeatherDataController();

    public ImportBulkWeatherDataUI() {
        controller.registerImporter(new CsvWeatherDataImporter());
    }

    @Override
    protected boolean doShow() {
        final List<String> formats = controller.availableFormats();

        if (formats.isEmpty()) {
            System.out.println("No import formats are currently available.");
            return false;
        }

        try {
            System.out.println("Available formats:");
            for (int i = 0; i < formats.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + formats.get(i));
            }

            final String formatChoice = Console.readNonEmptyLine(
                    "Choose format (1-" + formats.size() + "):", "Format is mandatory.");
            final int choice = Integer.parseInt(formatChoice.trim());
            if (choice < 1 || choice > formats.size()) {
                System.out.println("Invalid choice.");
                return false;
            }
            final String format = formats.get(choice - 1);

            final String filePath = Console.readNonEmptyLine(
                    "File path:", "File path is mandatory.");

            final List<WeatherDataImportResult> results = controller.importFromFile(filePath, format);

            final long successes = results.stream().filter(WeatherDataImportResult::success).count();
            final long failures = results.size() - successes;

            System.out.println();
            System.out.println("Import complete: " + successes + " imported, " + failures + " failed.");

            if (failures > 0) {
                System.out.println("Errors:");
                results.stream()
                        .filter(r -> !r.success())
                        .forEach(r -> System.out.println("  Line " + r.lineNumber() + ": " + r.errorMessage()));
            }

        } catch (final NumberFormatException e) {
            System.out.println("Invalid input.");
        } catch (final IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public String headline() {
        return "Import Bulk Weather Data";
    }
}