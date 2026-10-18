package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.application.RegisterFlightController;
import aiSafe.flight.domain.*;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.FlightRoute;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User interface class responsible for facilitating the registration of a new flight in the system. This class will interact with the user to gather necessary information about the engine model and then delegate the registration process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 1.0
 */

public class RegisterFlightUI extends AbstractUI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterFlightUI.class);

    private final RegisterFlightController theController;

    public RegisterFlightUI(){
        theController = new RegisterFlightController();
    }

    @Override
    protected boolean doShow() {
        final String routeName = Console.readNonEmptyLine("Route Name: ", "Route name is mandatory.");
        final String routeOrigin = Console.readNonEmptyLine("Route Origin: ", "Route origin is mandatory.");
        final String routeDestination = Console.readNonEmptyLine("Route Destination: ", "Route destination is mandatory.");
        final String airlineCode = Console.readNonEmptyLine("Airline Code (IATA): ", "Airline code is mandatory.");
        final int flightNumber = Console.readInteger("Flight Number: ");
        final String operationalSuffix = Console.readLine("Operational Suffix (optional): ");
        final FlightType fuelType = readEnumChoice("Fuel type", FlightType.class);
        final String aircraftRegistration = Console.readNonEmptyLine("Aircraft Registration: ", "Aircraft registration is mandatory.");
        try {
            FlightDesignator flightDesignator = theController.generateFlightDesignator(airlineCode, flightNumber, operationalSuffix);
            AircraftRegistration registration = AircraftRegistration.valueOf(aircraftRegistration);
            final String routePrefix = routeName.substring(0, 2).toUpperCase();
            final int routeNumber = Integer.parseInt(routeName.substring(2));
            RouteName routeNameEntity = RouteName.valueOf(routePrefix, routeNumber);
            RouteDesignator routeDesignator = RouteDesignator.valueOf(routeOrigin, routeDestination);
            FlightRoute route = new FlightRoute(routeNameEntity, routeDesignator);
            theController.registerFlight(route, flightDesignator, fuelType, registration);
            return true;
        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to register engine model: " + ex.getMessage());
        } catch (final Exception ex) {
            System.out.println("Unexpected error while registering engine model.");
        }
        return false;
    }

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


    @Override
    public String headline() {
        return "Register New Flight";
    }
}
