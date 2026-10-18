package aiSafe.persistence.impl.inmemory;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.airport.domain.Airport;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

public class InMemoryFlightRepository
        extends InMemoryDomainRepository<Flight, Long>
        implements FlightRepository {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    private final AirportRepository airportRepository;

    public InMemoryFlightRepository(final AirportRepository airportRepository) {
        if (airportRepository == null) {
            throw new IllegalArgumentException("AirportRepository cannot be null.");
        }
        this.airportRepository = airportRepository;
    }

    @Override
    public <S extends Flight> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final Flight entity) {
        try {
            final var field = Flight.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, SEQUENCE.getAndIncrement());
        } catch (final Exception ex) {
            throw new IllegalStateException(
                    "Unable to assign in-memory identity to Flight", ex);
        }
    }

    @Override
    public List<Flight> findByAircraftRegistration(
            final AircraftRegistration registration) {

        final Iterable<Flight> result = match(
                flight -> flight.aircraftRegistration() != null
                        && flight.aircraftRegistration().equals(registration)
        );

        return StreamSupport
                .stream(result.spliterator(), false)
                .toList();
    }

    @Override
    public Optional<Flight> findByDesignator(final aiSafe.flight.domain.FlightDesignator designator) {
        return Optional.ofNullable(matchOne(flight -> flight.designator().equals(designator)).orElse(null));
    }

    @Override
    public List<Flight> findByRoute(final FlightRoute route) {
        final Iterable<Flight> result = match(
                flight -> flight.route() != null
                        && flight.route().sameAs(route)
        );

        return StreamSupport
                .stream(result.spliterator(), false)
                .toList();
    }

    @Override
    public boolean hasActiveFlightsForPilot(
            final PilotID pilotId
    ) {

        return StreamSupport
                .stream(findAll().spliterator(), false)
                .flatMap(flight ->
                        flight.flightPlans().stream()
                )
                .anyMatch(plan ->
                        plan.pilotId().equals(pilotId)
                                &&
                                plan.isValidated()
                );
    }

    @Override
    public boolean hasOverlappingScheduleForPilot(
            final PilotID pilotId,
            final LocalDateTime departure,
            final LocalDateTime arrival,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId
    ) {
        return StreamSupport.stream(findAll().spliterator(), false)
                .anyMatch(flight -> flight.flightPlans().stream()
                        .filter(plan -> plan.pilotId() != null && plan.pilotId().equals(pilotId))
                        .filter(plan -> plan.isPending())
                        .filter(plan -> plan.arrivalDateTime() != null)
                        .filter(plan -> excludeFlightPlanId == null || !excludeFlightPlanId.equals(plan.planId()))
                        .filter(plan -> !isExcludedPendingOnFlight(plan, flight, excludePendingPlansOnFlightId))
                        .anyMatch(plan -> departure.isBefore(plan.arrivalDateTime().value())
                                && arrival.isAfter(plan.departureDateTime().value())));
    }

    private static boolean isExcludedPendingOnFlight(
            final aiSafe.flight.domain.FlightPlan plan,
            final Flight flight,
            final Long excludePendingPlansOnFlightId
    ) {
        if (excludePendingPlansOnFlightId == null || !plan.isPending()) {
            return false;
        }
        return excludePendingPlansOnFlightId.equals(flight.identity());
    }

    @Override
    public List<Flight> findByPeriodAndArea(final YearMonth period, final AreaCode areaCode) {
        if (period == null) throw new IllegalArgumentException("Period cannot be null.");
        if (areaCode == null) throw new IllegalArgumentException("Area code cannot be null.");

        final Set<String> airportCodesInArea = new HashSet<>();
        for (final Airport airport : airportRepository.findAll()) {
            if (airport.airControlArea() != null
                    && areaCode.equals(airport.airControlArea().areaCode())) {
                airportCodesInArea.add(airport.iataCode().value());
            }
        }

        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(flight -> belongsToArea(flight, airportCodesInArea))
                .filter(flight -> hasPlanDepartingInPeriod(flight, period))
                .toList();
    }

    private boolean belongsToArea(final Flight flight, final Set<String> airportCodesInArea) {
        if (flight.route() == null || flight.route().routeDesignator() == null) {
            return false;
        }
        return airportCodesInArea.contains(flight.route().routeDesignator().origin());
    }

    private boolean hasPlanDepartingInPeriod(final Flight flight, final YearMonth period) {
        return flight.flightPlans().stream()
                .anyMatch(plan -> YearMonth.from(plan.departureDateTime().value()).equals(period));
    }
}