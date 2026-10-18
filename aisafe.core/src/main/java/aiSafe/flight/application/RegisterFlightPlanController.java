package aiSafe.flight.application;

import aiSafe.flight.domain.Flight;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.Objects;

@UseCaseController
public class RegisterFlightPlanController {

    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory repositoryFactory;
    private final RegisterFlightPlanService registerFlightPlanService;

    private Flight validatedFlight;

    public RegisterFlightPlanController() {
        this(
                new AuthorizationGuardService(),
                PersistenceContext.repositories(),
                new RegisterFlightPlanService(PersistenceContext.repositories())
        );
    }

    RegisterFlightPlanController(
            final AuthorizationGuardService authorizationGuardService,
            final RepositoryFactory repositoryFactory,
            final RegisterFlightPlanService registerFlightPlanService
    ) {
        this.authorizationGuardService = Objects.requireNonNull(authorizationGuardService);
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
        this.registerFlightPlanService = Objects.requireNonNull(registerFlightPlanService);
    }

    public Flight validateFlight(
            final String airlineCode,
            final int flightNumber,
            final String operationalSuffix
    ) {
        authorizationGuardService.ensureAuthenticatedPilot();

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();
        try {
            validatedFlight = registerFlightPlanService.validateFlight(
                    airlineCode, flightNumber, operationalSuffix, tx);
            tx.commit();
            return validatedFlight;
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }

    public String validatedFlightAircraftRegistration() {
        if (validatedFlight == null) {
            throw new IllegalStateException("Validate the flight first.");
        }
        return validatedFlight.aircraftRegistration().toString();
    }

    public java.util.Optional<String> currentFlightPlanNotice() {
        if (validatedFlight == null) {
            return java.util.Optional.empty();
        }
        return FlightPlanReplaceabilityGuard.replacementNotice(validatedFlight);
    }

    public void validatePilot(final String pilotId) {
        authorizationGuardService.ensureAuthenticatedPilot();

        if (validatedFlight == null) {
            throw new IllegalStateException("Validate the flight before validating the pilot.");
        }

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();
        try {
            registerFlightPlanService.validatePilot(validatedFlight, pilotId, tx);
            tx.commit();
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }

    public Flight registerFlightPlan(final RegisterFlightPlanDTO dto) {
        authorizationGuardService.ensureAuthenticatedPilot();

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();
        try {
            final Flight result = registerFlightPlanService.registerFlightPlan(dto, tx);
            tx.commit();
            return result;
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }
}
