package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.flight.application.importplan.ImportFlightPlanFromFileController;
import aiSafe.flight.application.importplan.ImportFlightPlanResult;
import aiSafe.flight.domain.FlightDesignator;
import eapli.framework.io.util.Console;

import java.nio.file.Path;

public class ImportFlightPlanFromFileUI {

    private final ImportFlightPlanFromFileController controller =
            new ImportFlightPlanFromFileController();

    public boolean show() {
        try {
            System.out.println("=== Import Flight Plan ===");

            final String airlineCode = Console.readNonEmptyLine(
                    "Flight airline code (IATA): ",
                    "Airline code is mandatory."
            ).toUpperCase();
            final int flightNumber = Console.readInteger("Flight number: ");
            final String operationalSuffix = Console.readLine("Operational suffix (optional): ");
            final FlightDesignator flightId = controller.generateFlightDesignator(
                    airlineCode, flightNumber, operationalSuffix);

            final String pathValue = Console.readNonEmptyLine(
                    "Path to DSL file (.flight): ",
                    "File path is mandatory.");
            final Path path = Path.of(pathValue.trim());

            final ImportFlightPlanResult result = controller.importFlightPlanFromFile(flightId, path);

            if (result.isSuccess()) {
                System.out.println("Flight plan imported (status: DRAFT).");
                result.flightPlan().ifPresent(fp -> {
                    System.out.println("Route: " + fp.routeSignature());
                    System.out.println("Legs: " + fp.legCount());
                });
                System.out.println("Validate the plan when ready (US085).");
            } else {
                System.out.println("Import failed:");
                result.formattedErrors().forEach(error -> System.out.println(" - " + error));
            }

            return result.isSuccess();
        } catch (final Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }
}
