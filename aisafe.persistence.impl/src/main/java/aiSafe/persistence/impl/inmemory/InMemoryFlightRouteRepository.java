package aiSafe.persistence.impl.inmemory;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteName;
import aiSafe.flightRoute.repositories.FlightRouteRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.StreamSupport;

public class InMemoryFlightRouteRepository
        extends InMemoryDomainRepository<FlightRoute, Long>
        implements FlightRouteRepository {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends FlightRoute> S save(final S entity) {
        if (entity.identity() == null) {
            assignIdentity(entity);
        }
        return super.save(entity);
    }

    private void assignIdentity(final FlightRoute entity) {
        try {
            final var field = FlightRoute.class.getDeclaredField("routeID");
            field.setAccessible(true);
            field.set(entity, SEQUENCE.getAndIncrement());
        } catch (final Exception ex) {
            throw new IllegalStateException(
                    "Unable to assign in-memory identity to FlightRoute", ex);
        }
    }

    @Override
    public Optional<FlightRoute> findByRouteName(final RouteName routeName) {
        return matchOne(route -> route.routeName() != null && route.routeName().equals(routeName));
    }

    @Override
    public Iterable<FlightRoute> findActiveRoutesByCompanyAndDate(
            final AirTransportCompany company,
            final LocalDateTime currentDate
    ) {
        final Iterable<FlightRoute> result = match(route ->
                route.routeName() != null
                        && route.routeName().value().equals(company.iataCode().code())
                        && (route.deactivationDate() == null || route.deactivationDate().isAfter(currentDate))
        );
        return StreamSupport.stream(result.spliterator(), false).toList();
    }
}