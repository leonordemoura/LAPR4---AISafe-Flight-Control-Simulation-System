package aiSafe.flight.application;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flight.domain.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.Objects;

@UseCaseController
public class RegisterFlightController {
    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory repositoryFactory;

    public RegisterFlightController() {
        this(new AuthorizationGuardService(), PersistenceContext.repositories());
    }

    RegisterFlightController(
            final AuthorizationGuardService authorizationGuardService,
            final RepositoryFactory repositoryFactory
    ) {
        this.authorizationGuardService = Objects.requireNonNull(authorizationGuardService);
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
    }

    public boolean registerFlight(final FlightRoute route, final FlightDesignator designator, final FlightType type, final AircraftRegistration aircraftRegistration) {

        authorizationGuardService.ensureAuthenticatedPilot();

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();

        try {
            final var routeRepository = repositoryFactory.flightRoutes();

            final var existingRoute = routeRepository.findByRouteName(route.routeName());
            if (existingRoute.isEmpty()) {
                throw new IllegalStateException("No flight route found with the provided route name.");
            }
            if (!existingRoute.get().isActive()) {
                throw new IllegalStateException("Flight route is not active.");
            }

            final var repository = repositoryFactory.flights(tx);

            final var existingFlight = repository.findByDesignator(designator);
            if (existingFlight.isPresent()) {
                throw new IllegalStateException("An air transport company with the same ICAO code already exists.");
            }

            final var aircraftRepository = repositoryFactory.aircrafts(tx);

            final var existingAircraft = aircraftRepository.findByRegistration(aircraftRegistration.toString());
            if (existingAircraft.isEmpty()) {
                throw new IllegalStateException("A flight can not have a non-existing aircraft associated.");
            }


            final var newFlight = new Flight(existingRoute.get(), designator, type, aircraftRegistration);

            repository.save(newFlight);

            tx.commit();

            return true;
        } catch (final Exception ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }

    public FlightDesignator generateFlightDesignator(final String airlineIcaoCode, final int flightNumber, final String operationalSuffix) {
        authorizationGuardService.ensureAuthenticatedPilot();

        final  var repository = repositoryFactory.getAirTransportCompanyRepository();

        final var existingAirline = repository.findByIataCode(IataCode.valueOf(airlineIcaoCode));
        if(existingAirline.isEmpty()){
            throw new IllegalStateException("No air transport company found with the provided IATA code.");
        }

        return new FlightDesignator(airlineIcaoCode, flightNumber, operationalSuffix);
    }
}
