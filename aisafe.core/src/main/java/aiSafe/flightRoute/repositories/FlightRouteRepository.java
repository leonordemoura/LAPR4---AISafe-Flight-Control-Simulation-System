package aiSafe.flightRoute.repositories;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteName;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for managing FlightRoute entities.
 */
public interface FlightRouteRepository extends DomainRepository<Long, FlightRoute> {
    /**
     * Find the route by the unique identifier
     * @param routeName unique identifier of the route
     * @return a route with the name requested
     */
    Optional<FlightRoute> findByRouteName(RouteName routeName);

    /**
     * Find all active routes of a company in a specific date
     * @param company the company that operates the routes
     * @param currentDate the date to check if the route is active
     * @return a list of active routes of the company in the date requested
     */
    Iterable<FlightRoute> findActiveRoutesByCompanyAndDate(AirTransportCompany company, LocalDateTime currentDate);
}
