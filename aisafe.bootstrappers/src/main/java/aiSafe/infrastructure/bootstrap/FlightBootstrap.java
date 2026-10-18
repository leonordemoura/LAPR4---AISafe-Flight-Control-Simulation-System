package aiSafe.infrastructure.bootstrap;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.*;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.infrastructure.persistence.PersistenceContext;

import java.time.LocalDateTime;

public class FlightBootstrap {

    private final FlightRepository flightRepository = PersistenceContext.repositories().flights();
    private final FlightRouteRepository flightRouteRepository = PersistenceContext.repositories().flightRoutes();

    public void execute() {

        FlightRoute routeTP123 = flightRouteRepository
                .findByRouteName(RouteName.valueOf("TP", 123))
                .orElseThrow(() -> new IllegalStateException("FlightRoute TP123 not found — run FlightRouteBootstrap first"));

        Flight f1 = new Flight(
                routeTP123,
                new FlightDesignator("TP", 100, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA")
        );

        Flight f2 = new Flight(
                routeTP123,
                new FlightDesignator("TP", 200, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA")
        );

        Flight f3 = new Flight(
                routeTP123,
                new FlightDesignator("TP", 123, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA")
        );

        // US082 — f1 gets a DRAFT plan so the pilot can insert weather data
        final LocalDateTime departure = LocalDateTime.now().plusDays(3);
        final LocalDateTime arrival = departure.plusHours(2);

        f1.addFlightPlan(
                AircraftRegistration.valueOf("CS-TUA"),
                new PilotID("pilot@aisafe.local"),
                new DepartureDateTime(departure),
                new ArrivalDateTime(arrival, departure),
                new FuelQuantity(5000.0, "kg"),
                new DslSource("flight TP100 { leg { departure LPPT arrival LPPR } }")
        );

        flightRepository.save(f1);
        flightRepository.save(f2);
        flightRepository.save(f3);
    }
}