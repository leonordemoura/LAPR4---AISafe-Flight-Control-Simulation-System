package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaFlightRouteRepository
        extends JpaAutoTxRepository<FlightRoute, Long, Long>
        implements FlightRouteRepository {

    public JpaFlightRouteRepository(final TransactionalContext tx) {
        super(tx, "route_id");
    }

    public JpaFlightRouteRepository(final String puname) {
        super(
                puname,
                Application.settings().getExtendedPersistenceProperties(),
                "route_id"
        );
    }

    @Override
    public Optional<FlightRoute> findByRouteName(final RouteName routeName) {
        final Map<String, Object> params = new HashMap<>();
        params.put("value", routeName.value());
        params.put("routeNumber", routeName.routeNumber());

        return matchOne(
                "e.routeName.value = :value " +
                        "AND e.routeName.routeNumber = :routeNumber",
                params
        );
    }

    @Override
    public Iterable<FlightRoute> findActiveRoutesByCompanyAndDate(AirTransportCompany company, LocalDateTime currentDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("iataCode", company.iataCode().code());
        params.put("currentDate", currentDate);

        return match(
                "e.routeName.value = :iataCode " +
                        "AND (e.deactivationDate IS NULL OR e.deactivationDate > :currentDate)",
                params
        );
    }
}