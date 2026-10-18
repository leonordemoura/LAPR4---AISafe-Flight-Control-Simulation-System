package aiSafe.flightRoute.application;

import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.flight.domain.Flight;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for handling the deactivation of flight routes. It ensures that only authorized users can deactivate routes and that there are no scheduled flights departing after the deactivation date.
 */
public class DeactivateFlightRouteService {

    /** Repository factory to access the necessary repositories for flight routes, air transport companies, and flights. */
    private final RepositoryFactory repositoryFactory;

    /**
     * Constructor for the DeactivateFlightRouteService.
     * @param repositoryFactory the repository factory to access the necessary repositories for flight routes, air transport companies, and flights.
     */
    public DeactivateFlightRouteService(final RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    /**
     * Deactivates a flight route based on the provided DTO, deactivation date, and user email. It checks for the existence of the flight route, validates the user's authorization, ensures there are no scheduled flights departing after the deactivation date, and then deactivates the route if all conditions are met.
     * @param dto the data transfer object containing the flight route information to be deactivated.
     * @param deactivationDate the date and time when the flight route should be deactivated. It must be today or in the future.
     * @param userEmail the email of the authenticated user attempting to deactivate the flight route. The user must be a collaborator of the air transport company associated with the route prefix.
     * @param txCtx the transactional context to ensure that all operations are performed within a transaction.
     * @throws IllegalArgumentException if the flight route does not exist, if the route name format is invalid, if the deactivation date is invalid, or if the user is not authorized to deactivate the route.
     * @throws IllegalStateException if there are scheduled flights departing after the deactivation date or if the flight route does not belong to the authenticated user's company.
     */
    public void deactivateFlightRoute(final FlightRouteDTO dto, final LocalDateTime deactivationDate, final Email userEmail, final TransactionalContext txCtx) {

        final FlightRouteRepository flightRouteRepository = repositoryFactory.flightRoutes(txCtx);

        final String routePrefix = dto.routeName.substring(0, 2).toUpperCase();
        final int routeNumber = divideRouteName(dto.routeName, routePrefix);

        final AirTransportCompanyRepository airTransportCompanyRepository = repositoryFactory.getAirTransportCompanyRepository(txCtx);

        final var airTransportCompany = airTransportCompanyRepository
                .findByIataCode(IataCode.valueOf(routePrefix))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No air transport company found with the given IATA code prefix."));

        if (!airTransportCompany.hasCollaborator(userEmail)) {
            throw new IllegalStateException(
                    "The authenticated user is not a collaborator of the air transport company associated with the route prefix.");
        }

        final FlightRoute flightRoute = flightRouteRepository
                .findByRouteName(RouteName.valueOf(routePrefix, routeNumber))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No flight route found with the given route name."));

        if (!flightRoute.routeName().value().equals(airTransportCompany.iataCode().code())) {
            throw new IllegalStateException(
                    "The flight route does not belong to the authenticated user's company.");
        }

        validateDeactivationDate(deactivationDate);

        final var flightRepository = repositoryFactory.flights(txCtx);
        final List<Flight> flights = flightRepository.findByRoute(flightRoute);

        for (final Flight flight : flights) {
            if (flight.hasPlanDepartingAfter(deactivationDate)) {
                throw new IllegalStateException("Cannot deactivate the flight route because there are flights scheduled to depart after the deactivation date.");
            }
        }

        flightRoute.deactivate(deactivationDate);
        flightRouteRepository.save(flightRoute);
    }

    /**
     * Retrieves the active flight routes associated with the air transport company of the authenticated user. It checks for the user's authorization and returns only the active routes that belong to the user's company.
     * @param userEmail the email of the authenticated user for whom to retrieve the active flight routes. The user must be a collaborator of an air transport company.
     * @return an iterable of active flight routes associated with the air transport company of the authenticated user.
     * @throws IllegalArgumentException if no air transport company is found for the given user email or if the user is not a collaborator of any air transport company.
     */
    public Iterable<FlightRoute> getActiveRoutes(final Email userEmail) {
        final AirTransportCompanyRepository airTransportCompanyRepository = repositoryFactory.getAirTransportCompanyRepository();
        final var airTransportCompany = airTransportCompanyRepository.findByCollaboratorEmail(userEmail).orElseThrow(() -> new IllegalArgumentException("No air transport company found for the given user email."));

        final FlightRouteRepository flightRouteRepository = repositoryFactory.flightRoutes();
        return flightRouteRepository.findActiveRoutesByCompanyAndDate(airTransportCompany, LocalDateTime.now());
    }

    /**
     * Divides the route name into its prefix and number components. It validates that the route name follows the expected format of two uppercase letters followed by up to four digits, and that the route number is within the valid range of 1 to 9999.
     * @param routeName the full route name to be divided and validated. It must follow the format of two uppercase letters followed by up to four digits (e.g., "AA1234").
     * @param routePrefix the prefix of the route name, which should consist of the first two uppercase letters. It is used for validation purposes to ensure that the route name follows the expected format.
     * @return the route number extracted from the route name if the format is valid and the number is within the valid range; otherwise, it throws an IllegalArgumentException with an appropriate error message.
     */
    private int divideRouteName(final String routeName, final String routePrefix) {
        final String routeNumberString = routeName.substring(2);
        if (!routePrefix.matches("[A-Z]{2}") || !routeNumberString.matches("\\d{1,4}")) {
            throw new IllegalArgumentException("Route name must follow the format: two uppercase letters followed by up to four digits.");
        }

        final int routeNumber = Integer.parseInt(routeNumberString);
        if (routeNumber < 1 || routeNumber > 9999) {
            throw new IllegalArgumentException("Route number must be between 1 and 9999.");
        }
        return routeNumber;
    }

    /**
     * Validates the deactivation date to ensure that it is not null and that it is either today or in the future. If the deactivation date is invalid, it throws an IllegalArgumentException with an appropriate error message.
     * @param deactivationDate the date and time when the flight route should be deactivated. It must not be null and must be today or in the future.
     */
    private void validateDeactivationDate(final LocalDateTime deactivationDate) {
        if (deactivationDate == null) {
            throw new IllegalArgumentException("Deactivation date must not be null.");
        }
        if (deactivationDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Deactivation date must be today or in the future.");
        }
    }
}
