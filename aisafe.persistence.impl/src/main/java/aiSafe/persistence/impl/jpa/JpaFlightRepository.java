package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class JpaFlightRepository
        extends JpaAutoTxRepository<Flight, Long, Long>
        implements FlightRepository {

    public JpaFlightRepository(final TransactionalContext tx) {
        super(tx, "flightId");
    }

    public JpaFlightRepository(final String puname) {
        super(
                puname,
                Application.settings().getExtendedPersistenceProperties(),
                "flightId"
        );
    }

    @Override
    public List<Flight> findByAircraftRegistration(
            final AircraftRegistration registration) {

        final Map<String, Object> params = new HashMap<>();
        params.put("reg", registration.value());

        final Iterable<Flight> result = match(
                "e.aircraftRegistration.value = :reg",
                params
        );

        return StreamSupport.stream(result.spliterator(), false).toList();
    }

    @Override
    public Optional<Flight> findByDesignator(final aiSafe.flight.domain.FlightDesignator designator) {
        final Map<String, Object> params = new HashMap<>();
        params.put("airlineCode", designator.airlineCode());
        params.put("flightNumber", designator.flightNumber());
        params.put("operationalSuffix", designator.operationalSuffix());

        return matchOne(
                "e.designator.airlineCode = :airlineCode " +
                        "AND e.designator.flightNumber = :flightNumber " +
                        "AND e.designator.operationalSuffix = :operationalSuffix",
                params
        );
    }

    @Override
    public List<Flight> findByRoute(final FlightRoute route) {
        final Map<String, Object> params = new HashMap<>();
        params.put("routeId", route.identity());

        final Iterable<Flight> result = match(
                "e.route.routeID = :routeId",
                params
        );

        return StreamSupport.stream(result.spliterator(), false).toList();
    }

    @Override
    public boolean hasActiveFlightsForPilot(
            final PilotID pilotId
    ) {

        final var jpql =
                """
                
                        SELECT COUNT(fp)
                FROM FlightPlan fp
                WHERE fp.pilotId = :pilotId
                AND fp.status = aiSafe.flight.domain.FlightPlanStatus.VALIDATED
                """;

        final Long count =
                entityManager()
                        .createQuery(jpql, Long.class)
                        .setParameter("pilotId", pilotId)
                        .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean hasOverlappingScheduleForPilot(
            final PilotID pilotId,
            final LocalDateTime departure,
            final LocalDateTime arrival,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId
    ) {
        // Avoid "(:param IS NULL OR col <> :param)" — EclipseLink may bind null as varchar
        // and PostgreSQL rejects bigint <> varchar on flight_plan_id / flight_id.
        final var jpql = new StringBuilder(
                """
                SELECT COUNT(fp)
                FROM FlightPlan fp
                WHERE fp.pilotId = :pilotId
                AND fp.status IN (
                    aiSafe.flight.domain.FlightPlanStatus.DRAFT,
                    aiSafe.flight.domain.FlightPlanStatus.VALIDATED
                )
                AND fp.arrivalDateTime.value IS NOT NULL
                AND fp.departureDateTime.value < :arrival
                AND fp.arrivalDateTime.value > :departure
                """
        );
        if (excludeFlightPlanId != null) {
            jpql.append(" AND fp.id <> :excludePlanId");
        }
        if (excludePendingPlansOnFlightId != null) {
            jpql.append(
                    """
                     AND (
                        fp.flight.id <> :excludeFlightId
                        OR fp.status = aiSafe.flight.domain.FlightPlanStatus.VALIDATED
                    )
                    """
            );
        }

        final var query = entityManager()
                .createQuery(jpql.toString(), Long.class)
                .setParameter("pilotId", pilotId)
                .setParameter("departure", departure)
                .setParameter("arrival", arrival);
        if (excludeFlightPlanId != null) {
            query.setParameter("excludePlanId", excludeFlightPlanId);
        }
        if (excludePendingPlansOnFlightId != null) {
            query.setParameter("excludeFlightId", excludePendingPlansOnFlightId);
        }

        return query.getSingleResult() > 0;
    }

    @Override
    public List<Flight> findByPeriodAndArea(final YearMonth period, final AreaCode areaCode) {
        if (period == null) throw new IllegalArgumentException("Period cannot be null.");
        if (areaCode == null) throw new IllegalArgumentException("Area code cannot be null.");

        final LocalDateTime periodStart = period.atDay(1).atStartOfDay();
        final LocalDateTime periodEnd = period.plusMonths(1).atDay(1).atStartOfDay();

        return entityManager()
                .createQuery(
                        """
                        SELECT DISTINCT f
                        FROM Flight f
                        JOIN f.flightPlans fp
                        WHERE fp.departureDateTime.value >= :periodStart
                          AND fp.departureDateTime.value < :periodEnd
                          AND f.route.routeDesignator.origin IN (
                              SELECT a.iataCode.iataCode
                              FROM Airport a
                              WHERE a.airControlArea.areaCode.value = :areaCode
                          )
                        """, Flight.class)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .setParameter("areaCode", areaCode.value())
                .getResultList();
    }
}
