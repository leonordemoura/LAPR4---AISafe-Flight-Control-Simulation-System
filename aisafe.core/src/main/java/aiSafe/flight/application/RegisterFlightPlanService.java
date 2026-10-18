package aiSafe.flight.application;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import eapli.framework.domain.repositories.TransactionalContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class RegisterFlightPlanService {

    private final RepositoryFactory repositories;
    private final PilotScheduleConflictService pilotScheduleConflictService;

    public RegisterFlightPlanService(final RepositoryFactory repositories) {
        this.repositories = Objects.requireNonNull(repositories);
        this.pilotScheduleConflictService = new PilotScheduleConflictService(repositories);
    }

    public Flight validateFlight(
            final String airlineCode,
            final int flightNumber,
            final String operationalSuffix,
            final TransactionalContext tx
    ) {
        final String suffix = operationalSuffix == null || operationalSuffix.isBlank()
                ? null
                : operationalSuffix;
        final FlightDesignator designator = new FlightDesignator(airlineCode, flightNumber, suffix);

        final Flight flight = flightRepository(tx)
                .findByDesignator(designator)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found."));

        ensureRouteActiveAt(flight.route(), LocalDateTime.now());
        ensureFlightAircraftOperational(flight, tx);
        FlightPlanReplaceabilityGuard.ensureReplaceable(flight);

        return flight;
    }

    /**
     * Validates that the aircraft assigned to the flight exists and is active.
     */
    public Aircraft validateFlightAircraft(final Flight flight, final TransactionalContext tx) {
        return ensureFlightAircraftOperational(flight, tx);
    }

    public PilotCertification validatePilot(
            final Flight flight,
            final String pilotIdValue,
            final TransactionalContext tx
    ) {
        Objects.requireNonNull(flight, "Flight is required.");

        final PilotID pilotId = new PilotID(pilotIdValue == null ? "" : pilotIdValue.trim());

        final PilotCertification pilot = pilotCertificationRepository(tx)
                .findByPilotId(pilotId)
                .orElseThrow(() -> new IllegalArgumentException("Pilot certification not found."));

        if (!pilot.isActive()) {
            throw new IllegalArgumentException("Pilot is not active.");
        }

        final IataCode routeCompanyIata = IataCode.valueOf(flight.route().routeName().value());
        if (!routeCompanyIata.equals(pilot.company().iataCode())) {
            throw new IllegalArgumentException("Pilot does not belong to the route company.");
        }

        return pilot;
    }

    public Flight registerFlightPlan(final RegisterFlightPlanDTO dto, final TransactionalContext tx) {
        Objects.requireNonNull(dto, "Registration data is required.");

        final Flight flight = validateFlight(
                dto.flightAirlineCode,
                dto.flightNumber,
                dto.flightOperationalSuffix,
                tx);
        final AircraftRegistration aircraftRegistration = flight.aircraftRegistration();

        final PilotCertification pilot = validatePilot(flight, dto.pilotId, tx);

        final String dslText = resolveDslContent(dto);
        if (dto.dslContent != null && !dto.dslContent.isBlank()) {
            ensureDslFlightMatchesSelectedFlight(dslText, dto.flightDesignatorText());
            FlightPlanDslContentGuard.ensureMinimumStructure(dslText);
        }
        final FlightPlanSchedule schedule = FlightPlanScheduleResolver.fromRegisterDto(dto, dslText);
        ensureRouteActiveAt(flight.route(), schedule.departure());
        FlightPlanReplaceabilityGuard.ensureReplaceable(flight);

        final Long excludeDraftPlanId = flight.pendingReplaceablePlanId().orElse(null);
        final Long excludeFlightId = excludeDraftPlanId != null ? flight.identity() : null;
        pilotScheduleConflictService.ensureNoOverlap(
                pilot.identity(), schedule, excludeDraftPlanId, excludeFlightId, tx);

        final FlightPlan plan = flight.registerOrReplaceFlightPlan(
                aircraftRegistration,
                pilot.identity(),
                departureDateTimeForRegistration(schedule),
                arrivalDateTimeForRegistration(schedule),
                new FuelQuantity(dto.fuelQuantity, dto.fuelUnit),
                new DslSource(dslText)
        );
        applyRegistrationMetadata(plan, flight, dto);

        if (plan.status() != FlightPlanStatus.DRAFT) {
            throw new IllegalStateException("Newly created flight plan must be in DRAFT status.");
        }

        return flightRepository(tx).save(flight);
    }

    private FlightRepository flightRepository(final TransactionalContext tx) {
        return repositories.flights(tx);
    }

    private AircraftRepository aircraftRepository(final TransactionalContext tx) {
        return repositories.aircrafts(tx);
    }

    private PilotCertificationRepository pilotCertificationRepository(final TransactionalContext tx) {
        return repositories.pilots(tx);
    }

    private static DepartureDateTime departureDateTimeForRegistration(final FlightPlanSchedule schedule) {
        final LocalDateTime departure = schedule.departure();
        if (departure.isBefore(LocalDateTime.now())) {
            return DepartureDateTime.fromDslImport(departure);
        }
        return new DepartureDateTime(departure);
    }

    private static ArrivalDateTime arrivalDateTimeForRegistration(final FlightPlanSchedule schedule) {
        final LocalDateTime arrival = schedule.arrival();
        if (arrival.isBefore(LocalDateTime.now())) {
            return ArrivalDateTime.fromDslImport(arrival, schedule.departure());
        }
        return new ArrivalDateTime(arrival, schedule.departure());
    }

    private Aircraft ensureFlightAircraftOperational(final Flight flight, final TransactionalContext tx) {
        Objects.requireNonNull(flight, "Flight is required.");
        final String registration = flight.aircraftRegistration().toString();
        final Aircraft aircraft = aircraftRepository(tx)
                .findByRegistration(registration)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft assigned to the flight was not found."));

        if (!aircraft.isActive()) {
            throw new IllegalArgumentException("Aircraft assigned to the flight is decommissioned.");
        }

        final IataCode routeCompanyIata = IataCode.valueOf(flight.route().routeName().value());
        if (!routeCompanyIata.equals(aircraft.company().iataCode())) {
            throw new IllegalArgumentException("Aircraft does not belong to the same company as the route.");
        }

        return aircraft;
    }

    private static void ensureRouteActiveAt(final FlightRoute route, final LocalDateTime at) {
        Objects.requireNonNull(at, "Reference date/time is required.");
        if (!route.isActiveAt(at)) {
            throw new IllegalArgumentException("Flight route is not active.");
        }
    }

    private static void ensureDslFlightMatchesSelectedFlight(
            final String dslText,
            final String selectedFlightDesignator
    ) {
        final String dslFlight = FlightPlan.readDslFlightIdentifier(dslText);
        if (dslFlight == null || dslFlight.isBlank()) {
            throw new IllegalArgumentException(
                    "DSL must start with a flight block matching the selected flight "
                            + "(e.g. flight " + selectedFlightDesignator + " { ... }).");
        }
        if (!dslFlight.equalsIgnoreCase(selectedFlightDesignator.trim())) {
            throw new IllegalArgumentException(
                    "DSL flight identifier ("
                            + dslFlight
                            + ") does not match the selected flight ("
                            + selectedFlightDesignator
                            + ").");
        }
    }

    private static void applyRegistrationMetadata(
            final FlightPlan plan,
            final Flight flight,
            final RegisterFlightPlanDTO dto
    ) {
        String fallbackOrigin = null;
        String fallbackDestination = null;
        Integer fallbackLegCount = null;

        final List<RegisterFlightPlanLegInput> legs = dto.legs();
        if (!legs.isEmpty()) {
            fallbackOrigin = legs.getFirst().departureAirport;
            fallbackDestination = legs.getLast().arrivalAirport;
            fallbackLegCount = legs.size();
        } else {
            final var routeDesignator = flight.route().routeDesignator();
            fallbackOrigin = routeDesignator.origin();
            fallbackDestination = routeDesignator.destination();
            if (plan.legCount() == 0) {
                fallbackLegCount = 1;
            }
        }

        plan.applyRegistrationMetadata(
                dto.flightDesignatorText(),
                fallbackOrigin,
                fallbackDestination,
                fallbackLegCount);
    }

    private static String resolveDslContent(final RegisterFlightPlanDTO dto) {
        if (dto.dslContent != null && !dto.dslContent.isBlank()) {
            return dto.dslContent;
        }
        if (!dto.legs().isEmpty()) {
            final String flightType = dto.flightTypeForDsl == null || dto.flightTypeForDsl.isBlank()
                    ? "REGULAR"
                    : dto.flightTypeForDsl;
            return FlightPlanDslGenerator.generate(dto.flightDesignatorText(), flightType, dto.legs());
        }
        throw new IllegalArgumentException("DSL content or leg/segment data is required.");
    }
}
