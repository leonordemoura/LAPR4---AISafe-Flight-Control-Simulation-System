package aiSafe.app.backoffice.console.presentation.flightRoute;

import aiSafe.flightRoute.application.DeactivateFlightRouteController;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * UI class responsible for handling the user interface related to deactivating an existing flight route in the system. This class will interact with the user to allow them to select a flight route to deactivate and then delegate the deactivation process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 2.0
 */
public class DeactivateFlightRouteUI extends AbstractUI {
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeactivateFlightRouteUI.class);
    /** Controller for handling the deactivation of flight routes. */
    private final DeactivateFlightRouteController controller;

    /** Constructs a new DeactivateFlightRouteUI and initializes the controller. */
    public DeactivateFlightRouteUI() {
        controller = new DeactivateFlightRouteController();
    }

    /**
     * Displays the user interface for deactivating a flight route. The user will be prompted to select an active flight route and enter a deactivation date. The selected route will then be deactivated using the controller. Any business rule violations or unexpected errors will be handled gracefully with appropriate messages displayed to the user.
     * @return true if the operation was completed (regardless of success or failure), false if the user chose to cancel the operation.
     */
    @Override
    protected boolean doShow() {
        try {
            final FlightRoute route = readRoute("Select a flight route to deactivate:");
            final LocalDateTime deactivationDate = readDeactivationDate();
            final FlightRouteDTO dto = new FlightRouteDTO(route);
            controller.deactivateFlightRoute(dto, deactivationDate);
            System.out.println("Flight route '" + route.routeName() + "' deactivated successfully.");
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Unable to deactivate flight route: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }
        return true;
    }

    /**
     * Prompts the user to enter a deactivation date for the selected flight route. The date must be entered in the format "DD-MM-YYYY". The method will validate the input and convert it to a LocalDateTime object representing the start of the specified day. If the input is invalid, the user will be prompted again until a valid date is entered.
     * @return a LocalDateTime object representing the deactivation date entered by the user.
     */
    private LocalDateTime readDeactivationDate() {
        final Date date = Console.readDate("Enter deactivation date (DD-MM-YYYY):", "dd-MM-yyyy");
        final LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.atStartOfDay();
    }

    /**
     * Prompts the user to select an active flight route from the list of available routes. The method retrieves the active routes from the controller and displays them in a numbered list. The user must enter the number corresponding to the desired route. The method validates the input and returns the selected FlightRoute object. If there are no active routes available, an IllegalStateException is thrown.
     * @param label the label to display when prompting the user to select a flight route.
     * @return the FlightRoute object corresponding to the user's selection.
     */
    private FlightRoute readRoute(final String label) {
        final List<FlightRoute> routes = new ArrayList<>();
        controller.getActiveRoutes().forEach(routes::add);

        if (routes.isEmpty()) {
            throw new IllegalStateException(
                    "No active routes available. Please run bootstrap or register routes first.");
        }

        System.out.println(label);
        for (int i = 0; i < routes.size(); i++) {
            final FlightRoute route = routes.get(i);
            System.out.printf("  %d. %s (%s)%n", i + 1, route.routeName(), route.routeDesignator());
        }

        while (true) {
            final String input = Console.readNonEmptyLine(
                    label + " (1-" + routes.size() + "):", label + " is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= routes.size()) {
                    return routes.get(choice - 1);
                }
            } catch (final NumberFormatException ignored) {
                // fall through
            }
            System.out.println("Invalid choice. Please enter a number between 1 and " + routes.size() + ".");
        }
    }

    /**
     * Returns the headline to be displayed for this user interface. In this case, it indicates that the user is in the process of deactivating a flight route.
     * @return a String representing the headline for this user interface.
     */
    @Override
    public String headline() {
        return "Deactivate Flight Route";
    }
}
