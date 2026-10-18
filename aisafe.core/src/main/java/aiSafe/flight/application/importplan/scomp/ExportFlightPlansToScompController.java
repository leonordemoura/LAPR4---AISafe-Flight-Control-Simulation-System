package aiSafe.flight.application.importplan.scomp;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

/**
 * Exports validated flight plans to SCOMP JSON files.
 */
@UseCaseController
public class ExportFlightPlansToScompController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final ExportFlightPlansToScompService exportService =
            new ExportFlightPlansToScompService(PersistenceContext.repositories());

    public List<Flight> validatedFlights() {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
        return exportService.validatedFlights();
    }

    public ExportFlightPlansToScompResult export(final List<FlightDesignator> designators) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
        return exportService.export(designators);
    }
}
