package aiSafe.flightRoute.application;

import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.dto.FlightRouteDTO;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDateTime;

/**
 * Controller for the use case of deactivating a flight route. It ensures that the user has the necessary permissions and delegates the business logic to the DeactivateFlightRouteService.
 * @author 2DE - Group 3
 * @version 2.0
 */
@UseCaseController
public class DeactivateFlightRouteController {
    /** The service responsible for handling the business logic of deactivating flight routes. */
    private final DeactivateFlightRouteService service;
    /** The authorization service used to check user permissions. */
    private final AuthorizationService authz;
    /** The repository factory to access the necessary repositories for flight routes, air transport companies, and flights. */
    private final RepositoryFactory repositoryFactory;

    /**
     * Constructor for the DeactivateFlightRouteController. It initializes the service and authorization service.
     */
    public DeactivateFlightRouteController() {
        this.repositoryFactory = PersistenceContext.repositories();
        this.service = new DeactivateFlightRouteService(repositoryFactory);
        this.authz = AuthzRegistry.authorizationService();
    }

    /**
     * Deactivates a flight route based on the provided FlightRouteDTO and deactivation date. It first checks if the authenticated user has the necessary permissions, then retrieves the user's email and calls the service to perform the deactivation.
     * @param dto The Data Transfer Object containing the details of the flight route to be deactivated.
     * @param deactivationDate The date and time when the flight route should be deactivated.
     */
    public void deactivateFlightRoute(final FlightRouteDTO dto, final LocalDateTime deactivationDate) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final TransactionalContext txCtx = repositoryFactory.newTransactionalContext();

        txCtx.beginTransaction();
        try {
            final Email userEmail = new Email(authz.session().get().authenticatedUser().email().toString());
            service.deactivateFlightRoute(dto, deactivationDate, userEmail, txCtx);
            txCtx.commit();
        } catch (final Exception e) {
            txCtx.rollback();
            throw e;
        } finally {
            txCtx.close();
        }
    }

    /**
     * Retrieves the active flight routes for the authenticated user. It checks if the user has the necessary permissions and then calls the service to get the active routes.
     * @return An iterable collection of active FlightRoute objects associated with the authenticated user.
     */
    public Iterable<FlightRoute> getActiveRoutes() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        final Email userEmail = new Email(authz.session().get().authenticatedUser().email().toString());
        return service.getActiveRoutes(userEmail);
    }
}
