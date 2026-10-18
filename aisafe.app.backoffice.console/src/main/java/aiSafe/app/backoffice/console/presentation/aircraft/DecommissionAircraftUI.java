package aiSafe.app.backoffice.console.presentation.aircraft;

import aiSafe.aircraft.application.DecommissionAircraftController;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecommissionAircraftUI extends AbstractUI {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DecommissionAircraftUI.class);

    private final DecommissionAircraftController controller = new DecommissionAircraftController();

    @Override
    protected boolean doShow() {

        try {

            final String registration =
                    Console.readNonEmptyLine(
                            "Aircraft Registration: ",
                            "Registration cannot be empty");

            final String confirm =
                    Console.readNonEmptyLine(
                            "Confirm decommission? (yes/no): ",
                            "Please type yes or no");

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Operation cancelled.");
                return false;
            }

            controller.decommissionAircraft(registration);

            System.out.println(
                    "\nAircraft successfully decommissioned.");

        } catch (IllegalArgumentException ex) {

            System.out.println("\nError: " + ex.getMessage());

        } catch (IllegalStateException ex) {

            System.out.println(
                    "\nUnable to decommission: " + ex.getMessage());

        } catch (Exception ex) {

            LOGGER.error(
                    "Unexpected error while decommissioning aircraft",
                    ex);

            System.out.println(
                    "\nUnexpected error. Please contact support.");
        }

        return false;
    }

    @Override
    public String headline() {
        return "Decommission Aircraft (US071)";
    }
}