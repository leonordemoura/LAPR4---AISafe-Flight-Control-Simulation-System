package aiSafe.flightRoute.application;


import aiSafe.airport.domain.Airport;
import aiSafe.airport.domain.IataCode_Airport;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;

/**
 * Application service responsible for handling the business logic related to registering a new flight route in the system. This service interacts with the domain model and repositories to ensure that the flight route is created and persisted correctly, while also enforcing any necessary business rules and validations.
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
public class RegisterFlightRouteService {
    /** Repository factory used to access the necessary repositories for airports, air transport companies, and flight routes. This factory allows the service to interact with the underlying data storage and perform operations such as checking for the existence of airports, retrieving air transport company information, and saving new flight routes. By using a repository factory, the service can maintain a clean separation of concerns and adhere to the principles of dependency injection, which enhances the testability and maintainability of the code. */
    private final RepositoryFactory repositoryFactory;

    /** Constructor for the RegisterFlightRouteService class. It initializes the repository factory that will be used to interact with the repositories for airports, air transport companies, and flight routes. The repository factory is essential for accessing the necessary data and performing operations related to the registration of a new flight route. By using a repository factory, the service can maintain a clean separation of concerns and adhere to the principles of dependency injection, allowing for better testability and maintainability of the code. */
    public RegisterFlightRouteService(RepositoryFactory repositoryFactory) {
        this.repositoryFactory = repositoryFactory;
    }

    /**
     * Method responsible for registering a new flight route in the system. It takes a FlightRouteDTO object containing the necessary information about the flight route to be registered, as well as the email of the authenticated user who is attempting to register the route. The method performs several validations, such as checking if the origin and destination airports exist, if the route name follows the expected format, and if the authenticated user is a collaborator of the air transport company associated with the route prefix. If all validations pass, it creates a new FlightRoute object and saves it to the repository. The method also handles transactions to ensure data integrity during the registration process.
     * @param flightRouteDTO An object containing the necessary information about the flight route to be registered, such as the route name, origin IATA code, and destination IATA code. This DTO is used to transfer data from the user interface to the application service while ensuring that the data is structured and validated appropriately.
     * @param userEmail The email of the authenticated user who is attempting to register the flight route. This information is used to verify that the user has the necessary permissions to perform the registration, specifically checking if they are a collaborator of the air transport company associated with the route prefix. This ensures that only authorized users can register new flight routes in the system.
     * @param txCtx the transactional context to ensure that all operations are performed within a transaction.
     */
    public void registerFlightRoute(FlightRouteDTO flightRouteDTO, Email userEmail, TransactionalContext txCtx) {
        final var airportRepository = repositoryFactory.airports(txCtx);

        IataCode_Airport iataCodeOrigin = new IataCode_Airport(flightRouteDTO.originIataCode);
        if(!airportRepository.existsByIataCode(iataCodeOrigin)){
            throw new IllegalArgumentException("Origin airport with the given IATA code does not exist.");
        }

        IataCode_Airport iataCodeDestination = new IataCode_Airport(flightRouteDTO.destinationIataCode);
        if(!airportRepository.existsByIataCode(iataCodeDestination)){
            throw new IllegalArgumentException("Destination airport with the given IATA code does not exist.");
        }


        String routePrefix = flightRouteDTO.routeName.substring(0, 2).toUpperCase();
        int routeNumber = divideRouteName(flightRouteDTO.routeName, routePrefix);

        final var airTransportCompanyRepository = repositoryFactory.getAirTransportCompanyRepository(txCtx);

        final var airTransportCompany = airTransportCompanyRepository.findByIataCode(IataCode.valueOf(routePrefix))
                .orElseThrow(() -> new IllegalArgumentException("No air transport company found with the given IATA code prefix."));

        if (!airTransportCompany.hasCollaborator(userEmail)) {
            throw new IllegalStateException("The authenticated user is not a collaborator of the air transport company associated with the route prefix.");
        }

        final var flightRouteRepository = repositoryFactory.flightRoutes(txCtx);

        RouteName validRouteName = RouteName.valueOf(routePrefix, routeNumber);
        if (flightRouteRepository.findByRouteName(validRouteName).isPresent()) {
            throw new IllegalStateException("A flight route with the same name already exists.");
        }

        RouteDesignator routeDesignator = RouteDesignator.valueOf(flightRouteDTO.originIataCode, flightRouteDTO.destinationIataCode);

        final var newFlightRoute = new FlightRoute(validRouteName, routeDesignator);

        flightRouteRepository.save(newFlightRoute);
    }

    /**
     * Method responsible for validating and extracting the route number from the provided route name. The method checks if the route name follows the expected format (two uppercase letters followed by a number) and then extracts the numeric part to be used as the route number. If the format is invalid or if the route number is out of the acceptable range, the method will throw an IllegalArgumentException with an appropriate error message.
     * @param routeName The route name provided by the user, which is expected to follow the format of two uppercase letters followed by a number (e.g., "AB1234").
     * @param routePrefix The prefix of the route name, which consists of the first two uppercase letters that represent the Iata code of the air transport company.
     * @return An integer representing the route number extracted from the route name, which must be between 1 and 9999. If the route name is invalid, an exception will be thrown.
     */
    private int divideRouteName(String routeName, String routePrefix) {
        String routeNumberString = routeName.substring(2);
        if (!routePrefix.matches("[A-Z]{2}") || !routeNumberString.matches("\\d{1,4}")) {
            throw new IllegalArgumentException("Route name must follow the format: two uppercase letters followed by up to four digits.");
        }

        int routeNumber = Integer.parseInt(routeNumberString);
        if (routeNumber < 1 || routeNumber > 9999) {
            throw new IllegalArgumentException("Route number must be between 1 and 9999.");
        }
        return routeNumber;
    }

    /**
     * Method responsible for retrieving the list of available airports from the repository. This method interacts with the airport repository to fetch all the airports that are currently registered in the system, allowing other components of the application to access this information when needed, such as during the registration of a new flight route.
     * @return An iterable collection of Airport objects representing the available airports in the system. Each Airport object contains relevant information about the airport, such as its name, IATA code, and location. This method ensures that the application has access to up-to-date information about the airports, which is essential for the proper functioning of features related to flight routes and other airport-related operations.
     */
    public Iterable<Airport> getAvailableAirports() {
        return repositoryFactory.airports().findAll();
    }
}
