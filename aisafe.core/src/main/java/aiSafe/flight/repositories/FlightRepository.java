package aiSafe.flight.repositories;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.flightRoute.domain.FlightRoute;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends DomainRepository<Long, Flight> {

    List<Flight> findByAircraftRegistration(AircraftRegistration registration);

    List<Flight> findByRoute(FlightRoute route);

    Optional<Flight> findByDesignator(FlightDesignator designator);

    boolean hasActiveFlightsForPilot(PilotID pilotId);

    /**
     * @param excludeFlightPlanId plan being replaced (US081); ignored when null
     * @param excludePendingPlansOnFlightId when replacing on this flight, ignore its pending plans
     */
    boolean hasOverlappingScheduleForPilot(
            PilotID pilotId,
            LocalDateTime departure,
            LocalDateTime arrival,
            Long excludeFlightPlanId,
            Long excludePendingPlansOnFlightId);

    List<Flight> findByPeriodAndArea(YearMonth period, AreaCode areaCode);
}