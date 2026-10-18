package aiSafe.airport.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.airport.domain.*;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

@UseCaseController
public class RegisterAirportController {

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final RepositoryFactory factory =
            PersistenceContext.repositories();

    public Airport registerAirport(
            final String iata,
            final String icao,
            final String airportName,
            final String town,
            final String countryName,
            final String countryCode,
            final double elevation,
            final double latitude,
            final double longitude,
            final String areaCode
    ) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.BACKOFFICE_OPERATOR
        );

        final var txCtx = factory.newTransactionalContext();

        final var airportRepo =
                factory.airports(txCtx);

        final var areaRepo =
                factory.airControlAreas(txCtx);

        txCtx.beginTransaction();

        try {

            final var iataVO = new IataCode_Airport(iata);
            final var icaoVO = new IcaoCode_Airport(icao);

            if (airportRepo.existsByIataCode(iataVO)) {
                throw new IllegalArgumentException("IATA already exists");
            }

            if (airportRepo.existsByIcaoCode(icaoVO)) {
                throw new IllegalArgumentException("ICAO already exists");
            }

            final var area = areaRepo.findByAreaCode(
                    new AreaCode(areaCode)
            ).orElseThrow(() ->
                    new IllegalArgumentException("Air control area not found")
            );

            final var airportCoordinate =
                    new Coordinate_Airport(latitude, longitude);

            if (airportRepo.existsByCoordinate(airportCoordinate)) {
                throw new IllegalArgumentException(
                        "An airport already exists at these coordinates"
                );
            }

            final var boundaryCoordinate =
                    new aiSafe.aircontrolarea.domain.Coordinate(
                            latitude,
                            longitude
                    );

            if (!area.geoBoundary().contains(boundaryCoordinate)) {
                throw new IllegalArgumentException(
                        "Airport coordinates are outside the selected Air Control Area"
                );
            }

            final var airport = new Airport(
                    iataVO,
                    icaoVO,
                    new AirportName(airportName),
                    new Town(town),
                    new Country_Airport(countryName, countryCode),
                    new Elevation(elevation),
                    airportCoordinate,
                    area
            );

            final var saved = airportRepo.save(airport);

            txCtx.commit();
            return saved;

        } catch (Exception e) {
            txCtx.rollback();
            throw e;
        }
    }

    public Iterable<AirControlArea> allAirControlAreas() {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.BACKOFFICE_OPERATOR
        );

        final var txCtx = factory.newTransactionalContext();

        return factory.airControlAreas(txCtx).findAll();
    }

    public void validateIata(final String iata) {

        final var txCtx = factory.newTransactionalContext();

        final var airportRepo = factory.airports(txCtx);

        final var iataVO = new IataCode_Airport(iata);

        if (airportRepo.existsByIataCode(iataVO)) {
            throw new IllegalArgumentException(
                    "IATA already exists"
            );
        }
    }

    public void validateIcao(final String icao) {

        final var txCtx = factory.newTransactionalContext();

        final var airportRepo = factory.airports(txCtx);

        final var icaoVO = new IcaoCode_Airport(icao);

        if (airportRepo.existsByIcaoCode(icaoVO)) {
            throw new IllegalArgumentException(
                    "ICAO already exists"
            );
        }
    }

    public void validateCoordinates(
            final double latitude,
            final double longitude,
            final String areaCode
    ) {

        final var txCtx = factory.newTransactionalContext();

        final var area = factory.airControlAreas(txCtx)
                .findByAreaCode(new AreaCode(areaCode))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Air control area not found"
                        )
                );

        final var airportCoordinate =
                new aiSafe.airport.domain.Coordinate_Airport(
                        latitude,
                        longitude
                );

        final var areaCoordinate =
                new aiSafe.aircontrolarea.domain.Coordinate(
                        airportCoordinate.latitude(),
                        airportCoordinate.longitude()
                );

        if (!area.geoBoundary().contains(areaCoordinate)) {

            throw new IllegalArgumentException(
                    "Coordinates are outside selected area"
            );
        }
    }
}
