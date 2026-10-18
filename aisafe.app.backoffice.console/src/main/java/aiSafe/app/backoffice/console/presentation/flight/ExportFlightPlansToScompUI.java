package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.flight.application.importplan.scomp.ExportFlightPlanSelectionParser;
import aiSafe.flight.application.importplan.scomp.ExportFlightPlansToScompController;
import aiSafe.flight.application.importplan.scomp.ExportFlightPlansToScompResult;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flight.domain.FlightPlan;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ExportFlightPlansToScompUI extends AbstractUI {

    private final ExportFlightPlansToScompController controller = new ExportFlightPlansToScompController();

    @Override
    protected boolean doShow() {
        try {
            System.out.println("=== Export Flight Plans to SCOMP ===");

            final List<Flight> flights = controller.validatedFlights();
            if (flights.isEmpty()) {
                System.out.println("No validated flight plans available for export.");
                return false;
            }

            System.out.println("\nValidated flight plans available for export:");
            for (int i = 0; i < flights.size(); i++) {
                final Flight flight = flights.get(i);
                final FlightPlan plan = flight.flightPlan();
                System.out.printf(
                        "  %d. %-10s  route: %-12s  departure: %s  aircraft: %s%n",
                        i + 1,
                        flight.designator(),
                        plan.routeSignature() != null ? plan.routeSignature() : "N/A",
                        plan.departureDateTime() != null ? plan.departureDateTime().value() : "N/A",
                        plan.aircraftRegistration() != null ? plan.aircraftRegistration().value() : "N/A");
            }

            final String selection = Console.readNonEmptyLine(
                    "\nEnter flight numbers to export (comma-separated, or 'all'): ",
                    "Selection is mandatory.");

            final List<FlightDesignator> selectedDesignators =
                    ExportFlightPlanSelectionParser.parse(selection.trim(), flights);
            if (selectedDesignators.isEmpty()) {
                System.out.println("No flights selected.");
                return false;
            }

            final ExportFlightPlansToScompResult result = controller.export(selectedDesignators);
            if (!result.isSuccess()) {
                System.out.println("Export failed:");
                result.errors().forEach(error -> System.out.println(" - " + error));
                return false;
            }

            System.out.println("\nSCOMP JSON exported successfully.");
            System.out.println("File: " + result.outputPath());
            System.out.println("Flights exported (" + result.exportedFlightDesignators().size() + "):");
            result.exportedFlightDesignators().forEach(designator -> System.out.println(" - " + designator));
            return true;
        } catch (final IllegalArgumentException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Export Flight Plans to SCOMP";
    }
}
