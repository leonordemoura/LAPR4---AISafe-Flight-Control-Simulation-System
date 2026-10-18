package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

public class DecommissionAircraftController {

    private final RepositoryFactory factory = PersistenceContext.repositories();
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public DecommissionAircraftController() {
    }

    public void decommissionAircraft(final String registrationValue) {

        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final TransactionalContext txCtx = factory.newTransactionalContext();
        txCtx.beginTransaction();

        try {
            final AircraftRepository aircraftRepository = factory.aircrafts(txCtx);
            final AircraftRegistration registration =
                    AircraftRegistration.valueOf(registrationValue);

            final Aircraft aircraft = aircraftRepository
                    .findByRegistration(registrationValue)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "No aircraft found with registration: "
                                            + registrationValue));

            final FlightRepository flightRepository = factory.flights(txCtx);
            final List<Flight> flights =
                    flightRepository.findByAircraftRegistration(registration);

            final boolean hasPendingFlights = flights.stream()
                    .anyMatch(Flight::isActiveOrPending);

            if (hasPendingFlights) {
                throw new IllegalStateException(
                        "Aircraft has pending or active flights.");
            }

            aircraft.decommission();

            aircraftRepository.save(aircraft);
            txCtx.commit();
        } catch (final Exception ex) {
            txCtx.rollback();
            throw ex;
        }
    }
}