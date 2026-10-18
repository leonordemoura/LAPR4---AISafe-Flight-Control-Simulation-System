package aiSafe.infrastructure.bootstrap;

import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;

/**
 * Bootstrapper for FlightRoute entities. It checks if specific flight routes exist and creates them if they don't.
 */
public class FlightRouteBootstrap {
    /** Repository for managing FlightRoute entities. It is initialized using the PersistenceContext to access the appropriate repository implementation. */
    private final FlightRouteRepository repository = PersistenceContext.repositories().flightRoutes();

    /**
     * Executes the bootstrap process, ensuring that specific flight routes are registered in the system. It checks for the existence of each route and creates it if it is missing.
     */
    public void execute() {
        registerIfMissing("TP", 123, "OPO", "LIS");
        registerIfMissing("FP", 134, "LIS", "MAD");
        registerIfMissing("GG", 543, "OPO", "MAD");
    }

    /**
     * Helper method to register a flight route if it does not already exist in the repository. It constructs the RouteName and RouteDesignator based on the provided parameters, checks for the existence of the route, and saves it if it is missing. If the route already exists, it logs a message and skips the creation process.
     * @param prefix The IATA code prefix of the associated air transport company, which is part of the route name (e.g., "TP" for TAP Air Portugal).
     * @param number The numeric identifier for the route, which is combined with the prefix to form the complete route name (e.g., "123" in "TP123").
     * @param origin The IATA code of the origin airport for the flight route (e.g., "OPO" for Porto Airport).
     * @param destination The IATA code of the destination airport for the flight route (e.g., "LIS" for Lisbon Airport).
     */
    private void registerIfMissing(final String prefix, final int number, String origin, String destination) {

        final RouteName routeName = RouteName.valueOf(prefix, number);

        final RouteDesignator routeDesignator = RouteDesignator.valueOf(origin, destination);

        if (repository.findByRouteName(routeName).isPresent()) {
            System.out.println("FlightRoute " + routeName + " already exists. Skipping.");
            return;
        }

        final FlightRoute flightRoute = new FlightRoute(routeName, routeDesignator);

        try {
            repository.save(flightRoute);
            System.out.println("FlightRoute created: " + routeName);
        } catch (final Exception ex) {
            System.out.println("Error creating FlightRoute " + routeName);
        }
    }
}
