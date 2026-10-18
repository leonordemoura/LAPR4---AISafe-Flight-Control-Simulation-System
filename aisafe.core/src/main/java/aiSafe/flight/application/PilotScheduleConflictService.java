package aiSafe.flight.application;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import eapli.framework.domain.repositories.TransactionalContext;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Ensures a pilot has no overlapping flight plans in DRAFT or VALIDATED status.
 */
public final class PilotScheduleConflictService {

    private final RepositoryFactory repositories;

    public PilotScheduleConflictService(final RepositoryFactory repositories) {
        this.repositories = Objects.requireNonNull(repositories);
    }

    public void ensureNoOverlap(
            final PilotID pilotId,
            final FlightPlanSchedule schedule,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId
    ) {
        ensureNoOverlap(
                pilotId,
                schedule.departure(),
                schedule.arrival(),
                excludeFlightPlanId,
                excludePendingPlansOnFlightId);
    }

    public void ensureNoOverlap(
            final PilotID pilotId,
            final FlightPlanSchedule schedule,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId,
            final TransactionalContext tx
    ) {
        ensureNoOverlap(
                pilotId,
                schedule.departure(),
                schedule.arrival(),
                excludeFlightPlanId,
                excludePendingPlansOnFlightId,
                tx);
    }

    public void ensureNoOverlap(
            final PilotID pilotId,
            final LocalDateTime departure,
            final LocalDateTime arrival,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId
    ) {
        Objects.requireNonNull(pilotId);
        new FlightPlanSchedule(departure, arrival);

        if (flightRepository().hasOverlappingScheduleForPilot(
                pilotId, departure, arrival, excludeFlightPlanId, excludePendingPlansOnFlightId)) {
            throw new IllegalArgumentException(
                    "Pilot already has a flight plan that overlaps this schedule.");
        }
    }

    public void ensureNoOverlap(
            final PilotID pilotId,
            final LocalDateTime departure,
            final LocalDateTime arrival,
            final Long excludeFlightPlanId,
            final Long excludePendingPlansOnFlightId,
            final TransactionalContext tx
    ) {
        Objects.requireNonNull(pilotId);
        new FlightPlanSchedule(departure, arrival);

        if (flightRepository(tx).hasOverlappingScheduleForPilot(
                pilotId, departure, arrival, excludeFlightPlanId, excludePendingPlansOnFlightId)) {
            throw new IllegalArgumentException(
                    "Pilot already has a flight plan that overlaps this schedule.");
        }
    }

    private FlightRepository flightRepository() {
        return repositories.flights();
    }

    private FlightRepository flightRepository(final TransactionalContext tx) {
        return repositories.flights(tx);
    }
}
