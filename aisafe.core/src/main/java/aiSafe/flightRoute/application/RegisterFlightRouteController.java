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
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Controller class responsible for handling the user interactions and orchestrating the application logic related to registering a new flight route in the system. This controller interacts with the RegisterFlightRouteService to perform the necessary operations for validating the input data, checking user permissions, and ultimately registering the flight route. It also provides a method to retrieve the list of available airports, which is essential for ensuring that the flight route being registered has valid origin and destination airports.
 * @author 2DE - Group 3
 * @version 1.0
 */
@UseCaseController
public class RegisterFlightRouteController {
    /** Authorization service used to ensure that the authenticated user has the necessary permissions to register a new flight route. This service checks if the user has the appropriate role, specifically if they are a collaborator of an air transport company, before allowing them to proceed with the registration process. By enforcing these permissions, the controller helps maintain the security and integrity of the application, ensuring that only authorized users can perform sensitive operations such as registering new flight routes. */
    private final AuthorizationService authz;
    /** Service responsible for handling the business logic related to registering a new flight route. This service interacts with the domain model and repositories to ensure that the flight route is created and persisted correctly, while also enforcing any necessary business rules and validations. The controller delegates the registration process to this service after validating the user's permissions and gathering the necessary input data from the user interface. By using a dedicated service for this functionality, the controller can maintain a clean separation of concerns and adhere to the principles of single responsibility, allowing for better maintainability and testability of the code. */
    private final RegisterFlightRouteService service;

    /** Constructor for the RegisterFlightRouteController class. It initializes the authorization service and the RegisterFlightRouteService that will be used to handle the registration of a new flight route. The constructor retrieves the necessary repository factory from the persistence context and uses it to create an instance of the RegisterFlightRouteService, which will be responsible for executing the business logic related to flight route registration. By setting up these dependencies in the constructor, the controller ensures that it has access to the necessary services and repositories to perform its functions effectively. */
    public RegisterFlightRouteController() {
        RepositoryFactory repositoryFactory = PersistenceContext.repositories();
        this.authz = AuthzRegistry.authorizationService();
        this.service = new RegisterFlightRouteService(repositoryFactory);
    }

    /** Method responsible for registering a new flight route in the system. It takes a FlightRouteDTO object containing the necessary information about the flight route to be registered and performs several operations to ensure that the registration process is executed correctly. First, it checks if the authenticated user has the required permissions to register a flight route by verifying if they are a collaborator of an air transport company. If the user is authorized, it retrieves the email of the authenticated user from the session and passes it along with the FlightRouteDTO to the RegisterFlightRouteService, which will handle the actual registration process, including validations and interactions with the repositories. This method ensures that only authorized users can register new flight routes and that all necessary information is provided for a successful registration.
     * @param flightRouteDTO An object containing the necessary information about the flight route to be registered, such as the route name, origin IATA code, and destination IATA code. This DTO is used to transfer data from the user interface to the application service while ensuring that the data is structured and validated appropriately. The method will use this information to create a new flight route in the system, provided that all validations pass and the authenticated user has the necessary permissions to perform the registration.
     */
    public void registerFlightRoute(FlightRouteDTO flightRouteDTO) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();

        txCtx.beginTransaction();

        try {
        final Email userEmail = new Email(authz.session().get().authenticatedUser().email().toString());
        service.registerFlightRoute(flightRouteDTO, userEmail, txCtx);
            txCtx.commit();
        } catch (final Exception e) {
            txCtx.rollback();
            throw e;
        } finally {
            txCtx.close();
        }
    }

    /**
     * Method responsible for retrieving the list of available airports from the repository. This method interacts with the airport repository to fetch all the airports that are currently registered in the system, allowing other components of the application to access this information when needed, such as during the registration of a new flight route.
     * @return An iterable collection of Airport objects representing the available airports in the system. Each Airport object contains relevant information about the airport, such as its name, IATA code, and location. This method ensures that the application has access to up-to-date information about the airports, which is essential for the proper functioning of features related to flight routes and other airport-related operations.
     */
    public Iterable<Airport> getAvailableAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        return service.getAvailableAirports();
    }
}
