package aiSafe.app.backoffice.console.presentation.flightRoute;

import aiSafe.airport.domain.Airport;
import aiSafe.flightRoute.application.RegisterFlightRouteController;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Console UI for the "Register Flight Route" use case (US073).
 *
 * <p>Prompts the authenticated Air Transport Company Collaborator to select an origin and destination airport from the available list, then to enter a route name. Input is packaged into a {@link FlightRouteDTO} and forwarded to the {@link RegisterFlightRouteController}.</p>
 *
 * @author 2DE - Group 3
 * @version 2.0
 * @see RegisterFlightRouteController
 * @see FlightRouteDTO
 */
public class RegisterFlightRouteUI extends AbstractUI {
    /** Logger for debugging and error reporting. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterFlightRouteUI.class);

    /** The controller that handles the business logic for registering a flight route. */
    private final RegisterFlightRouteController controller;

    /**
     * Constructs the UI and initializes its controller.
     */
    public RegisterFlightRouteUI() {
        controller = new RegisterFlightRouteController();
    }

    /**
     * Collects user input and registers the flight route.
     *
     * <p>Presents numbered lists for origin and destination airport selection, then prompts for a route name. Catches and displays validation errors without crashing the menu loop.</p>
     *
     * @return {@code true} always, so the menu loop continues after this action
     */
    @Override
    protected boolean doShow() {
        final String origin = readAirport("Departure Airport IATA Code:");
        final String destination = readAirport("Arrival Airport IATA Code:");
        final String routeName = Console.readNonEmptyLine("Route name:", "Route name is mandatory.");

        try {
            final FlightRouteDTO flightRouteDTO = new FlightRouteDTO(routeName, origin, destination);
            controller.registerFlightRoute(flightRouteDTO);
            System.out.println("Flight route '" + routeName + "' registered successfully.");
        } catch (final IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Unable to register flight route: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error: " + ex.getMessage());
        }

        return true;
    }

    /**
     * Displays a numbered list of available airports and returns the IATA code of the one chosen by the user.
     *
     * <p>Keeps prompting until a valid numeric selection is made.</p>
     *
     * @param label context label shown to the user (e.g. {@code "Departure Airport IATA Code:"})
     * @return the IATA code string of the selected airport
     * @throws IllegalStateException if no airports are registered in the system
     */
    private String readAirport(final String label) {
        final List<Airport> airports = new ArrayList<>();
        controller.getAvailableAirports().forEach(airports::add);

        if (airports.isEmpty()) {
            throw new IllegalStateException("No airports available. Please run bootstrap or register airports first.");
        }

        System.out.println(label + " options:");
        for (int i = 0; i < airports.size(); i++) {
            final Airport a = airports.get(i);
            System.out.printf("  %d. %s (%s)%n", i + 1, a.airportName(), a.iataCode());
        }

        while (true) {
            final String input = Console.readNonEmptyLine(
                    label + " (1-" + airports.size() + "):", label + " is mandatory.");
            try {
                final int choice = Integer.parseInt(input.trim());
                if (choice >= 1 && choice <= airports.size()) {
                    return airports.get(choice - 1).iataCode().toString();
                }
            } catch (final NumberFormatException ignored) {
                // fall through to error message below
            }
            System.out.println("Invalid choice. Please enter a number between 1 and " + airports.size() + ".");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return the screen headline {@code "Register Flight Route"}
     */
    @Override
    public String headline() {
        return "Register Flight Route";
    }
}