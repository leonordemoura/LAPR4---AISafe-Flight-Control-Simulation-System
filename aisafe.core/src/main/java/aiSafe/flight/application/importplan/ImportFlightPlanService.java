package aiSafe.flight.application.importplan;

import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import aiSafe.aircraft.domain.Aircraft;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flight.application.FlightPlanReplaceabilityGuard;
import aiSafe.flight.application.FlightPlanSchedule;
import aiSafe.flight.application.FlightPlanScheduleResolver;
import aiSafe.flight.application.PilotScheduleConflictService;
import aiSafe.flight.domain.*;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.infrastructure.authz.application.AuthorizationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * US121 – persist imported DSL with operational fields (plan remains DRAFT until activation).
 */
final class ImportFlightPlanService {

    private final RepositoryFactory repositories;
    private final AuthorizationService authz;
    private final PilotScheduleConflictService pilotScheduleConflictService;

    ImportFlightPlanService(
            final RepositoryFactory repositories,
            final AuthorizationService authz
    ) {
        this.repositories = Objects.requireNonNull(repositories);
        this.authz = Objects.requireNonNull(authz);
        this.pilotScheduleConflictService = new PilotScheduleConflictService(repositories);
    }

    ImportFlightPlanService(
            final RepositoryFactory repositories,
            final AuthorizationService authz,
            final PilotScheduleConflictService pilotScheduleConflictService
    ) {
        this.repositories = Objects.requireNonNull(repositories);
        this.authz = Objects.requireNonNull(authz);
        this.pilotScheduleConflictService = Objects.requireNonNull(pilotScheduleConflictService);
    }

    ImportFlightPlanResult importPlan(
            final FlightDesignator flightId,
            final DslFlightPlan dslPlan,
            final String rawDslContent
    ) {
        final FlightRepository flightRepository = repositories.flights();
        final Optional<Flight> flightOptional = flightRepository.findByDesignator(flightId);
        if (flightOptional.isEmpty()) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.FLIGHT_NOT_FOUND,
                            "Flight not found: " + flightId)));
        }

        final Flight flight = flightOptional.get();
        FlightPlanReplaceabilityGuard.ensureReplaceable(flight);

        if (!dslPlan.identifier().equals(flightId.toString())) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.FLIGHT_ID_MISMATCH,
                            "Flight ID in file (" + dslPlan.identifier()
                                    + ") does not match selected flight (" + flightId + ")")));
        }

        final PilotCertification pilot;
        try {
            ensureFlightAircraftOperational(flight);
            pilot = resolveAuthenticatedPilot(flight);
        } catch (final IllegalArgumentException ex) {
            return ImportFlightPlanResult.failure(List.of(
                    mapPilotOrAircraftError(ex)));
        }

        final FlightPlanSchedule schedule;
        final DepartureDateTime departure;
        final ArrivalDateTime arrival;
        final FuelQuantity fuel;
        try {
            schedule = FlightPlanScheduleResolver.fromDslFlightPlan(dslPlan);
            departure = DepartureDateTime.fromDslImport(schedule.departure());
            arrival = ArrivalDateTime.fromDslImport(schedule.arrival(), schedule.departure());
            fuel = new FuelQuantity(dslPlan.legs().getFirst().fuel().amount(), dslPlan.legs().getFirst().fuel().unit());
            ensureRouteActiveAt(flight.route(), departure.value());
        } catch (final IllegalArgumentException ex) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(ImportFlightPlanErrorCode.ROUTE_MISMATCH, ex.getMessage())));
        }

        final Long excludeDraftPlanId = flight.pendingReplaceablePlanId().orElse(null);
        try {
            pilotScheduleConflictService.ensureNoOverlap(
                    pilot.identity(),
                    schedule,
                    excludeDraftPlanId,
                    flight.identity());
        } catch (final IllegalArgumentException ex) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.PILOT_SCHEDULE_CONFLICT,
                            ex.getMessage())));
        }

        final FlightPlan imported = FlightPlan.fromDslImport(
                dslPlan,
                rawDslContent,
                flight.aircraftRegistration(),
                pilot.identity(),
                departure,
                arrival,
                fuel);

        try {
            flight.importFlightPlan(imported);
        } catch (final IllegalStateException ex) {
            return ImportFlightPlanResult.failure(List.of(
                    mapLockedPlanError(ex)));
        } catch (final IllegalArgumentException ex) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.ROUTE_MISMATCH,
                            ex.getMessage())));
        }

        final Flight saved = flightRepository.save(flight);
        return ImportFlightPlanResult.success(saved, saved.flightPlan());
    }

    private static ImportFlightPlanError mapLockedPlanError(final IllegalStateException ex) {
        final String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.contains("VALIDATED")) {
            return new ImportFlightPlanError(
                    ImportFlightPlanErrorCode.FLIGHT_PLAN_NOT_REPLACEABLE,
                    message);
        }
        return new ImportFlightPlanError(
                ImportFlightPlanErrorCode.FLIGHT_PLAN_NOT_REPLACEABLE,
                message);
    }

    private static ImportFlightPlanError mapPilotOrAircraftError(final IllegalArgumentException ex) {
        final String message = ex.getMessage() == null ? "" : ex.getMessage();
        if (message.contains("Pilot") || message.contains("pilot") || message.contains("session")) {
            return new ImportFlightPlanError(ImportFlightPlanErrorCode.PILOT_NOT_FOUND, message);
        }
        return new ImportFlightPlanError(ImportFlightPlanErrorCode.ROUTE_MISMATCH, message);
    }

    private PilotCertification resolveAuthenticatedPilot(final Flight flight) {
        final String emailText = authz.session()
                .orElseThrow(() -> new IllegalArgumentException("No authenticated session."))
                .authenticatedUser()
                .email()
                .toString();

        final PilotCertificationRepository pilotRepository = repositories.pilots();
        final PilotCertification pilot = pilotRepository.findByUserEmail(new Email(emailText))
                .filter(PilotCertification::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user is not an active pilot."));

        final IataCode routeCompanyIata = IataCode.valueOf(flight.route().routeName().value());
        if (!routeCompanyIata.equals(pilot.company().iataCode())) {
            throw new IllegalArgumentException("Pilot does not belong to the route company.");
        }

        return pilot;
    }

    private void ensureFlightAircraftOperational(final Flight flight) {
        final String registration = flight.aircraftRegistration().toString();
        final Aircraft aircraft = repositories.aircrafts()
                .findByRegistration(registration)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft assigned to the flight was not found."));

        if (!aircraft.isActive()) {
            throw new IllegalArgumentException("Aircraft assigned to the flight is decommissioned.");
        }

        final IataCode routeCompanyIata = IataCode.valueOf(flight.route().routeName().value());
        if (!routeCompanyIata.equals(aircraft.company().iataCode())) {
            throw new IllegalArgumentException("Aircraft does not belong to the same company as the route.");
        }
    }

    private static void ensureRouteActiveAt(final FlightRoute route, final LocalDateTime at) {
        if (!route.isActiveAt(at)) {
            throw new IllegalArgumentException("Flight route is not active at departure time.");
        }
    }
}
