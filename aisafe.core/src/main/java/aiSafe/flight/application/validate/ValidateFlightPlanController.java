package aiSafe.flight.application.validate;

import aiSafe.flight.domain.FlightDesignator;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Validates the current DRAFT flight plan (US085).
 */
@UseCaseController
public class ValidateFlightPlanController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final ValidateFlightPlanService validateService =
            new ValidateFlightPlanService(PersistenceContext.repositories());

    public ValidateFlightPlanResult validateFlightPlan(final FlightDesignator flightId) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
        return validateService.validate(flightId);
    }
}
