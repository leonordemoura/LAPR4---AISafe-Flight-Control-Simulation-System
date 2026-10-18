package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.flight.application.validate.ValidateFlightPlanController;
import aiSafe.flight.application.validate.ValidateFlightPlanResult;
import aiSafe.flight.domain.FlightDesignator;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ValidateFlightPlanUI extends AbstractUI {

    private final ValidateFlightPlanController controller = new ValidateFlightPlanController();

    @Override
    protected boolean doShow() {
        try {
            System.out.println("=== Validate Flight Plan ===");

            final String airlineCode = Console.readNonEmptyLine(
                    "Flight airline code (IATA): ",
                    "Airline code is mandatory."
            ).toUpperCase();
            final int flightNumber = Console.readInteger("Flight number: ");
            final String operationalSuffix = Console.readLine("Operational suffix (optional): ");
            final FlightDesignator designator = new FlightDesignator(
                    airlineCode,
                    flightNumber,
                    operationalSuffix == null || operationalSuffix.isBlank() ? null : operationalSuffix.trim());

            System.out.println("Validating flight plan...");
            final ValidateFlightPlanResult result = controller.validateFlightPlan(designator);

            if (result.isSuccess()) {
                result.summary()
                        .map(summary -> summary.formattedLines())
                        .orElse(List.of("Flight plan validated (status: VALIDATED)."))
                        .forEach(System.out::println);
                return true;
            }

            System.out.println("Validation failed:");
            result.formattedErrors().forEach(msg -> System.out.println(" - " + msg));
            return false;
        } catch (final Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Validate Flight Plan";
    }
}
