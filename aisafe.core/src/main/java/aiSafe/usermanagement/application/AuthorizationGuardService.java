package aiSafe.usermanagement.application;

import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

public class AuthorizationGuardService {
    private final AuthorizationService authz;

    public AuthorizationGuardService() {
        this(AuthzRegistry.authorizationService());
    }

    public AuthorizationGuardService(final AuthorizationService authorizationService) {
        this.authz = authorizationService;
    }

    public void ensureAuthenticatedBackofficeUser() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR);
    }

    public void ensureAuthenticatedAdmin() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN);
    }

    public void ensureAuthenticatedPilot() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
    }

    public void ensureAuthenticatedFlightControlOperator() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.FLIGHT_CONTROL_OPERATOR);
    }
}
