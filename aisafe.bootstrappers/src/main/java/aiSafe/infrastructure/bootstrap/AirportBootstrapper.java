package aiSafe.infrastructure.bootstrap;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.airport.domain.*;
import aiSafe.airport.repositories.AirportRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

public class AirportBootstrapper {

    public void execute() {
        registerIfMissing(
                "LIS",
                "LPPT",
                "Lisbon Airport",
                "Lisbon",
                "Portugal",
                "PT",
                114,
                38.7742,
                -9.1342,
                "LPPT"
        );

        registerIfMissing(
                "OPO",
                "LPPR",
                "Porto Airport",
                "Porto",
                "Portugal",
                "PT",
                69,
                41.2481,
                -8.6814,
                "LPPT"
        );
    }

    private void registerIfMissing(
            final String iata,
            final String icao,
            final String name,
            final String town,
            final String countryName,
            final String countryCode,
            final double elevation,
            final double latitude,
            final double longitude,
            final String areaCodeValue
    ) {

        final var factory = PersistenceContext.repositories();

        final var airportRepo = factory.airports();
        final var areaRepo = factory.airControlAreas();

        final var iataVO = new IataCode_Airport(iata);
        final var icaoVO = new IcaoCode_Airport(icao);

        final var areaCode = new AreaCode(areaCodeValue);
        final var area = areaRepo.findByAreaCode(areaCode)
                .orElseThrow(() ->
                        new IllegalStateException("Air Control Area not found: " + areaCodeValue)
                );

        if (airportRepo.existsByIataCode(iataVO)) {
            System.out.println("Airport already exists (IATA): " + iata);
            return;
        }

        if (airportRepo.existsByIcaoCode(icaoVO)) {
            System.out.println("Airport already exists (ICAO): " + icao);
            return;
        }

        final var airport = new Airport(
                iataVO,
                icaoVO,
                new AirportName(name),
                new Town(town),
                new Country_Airport(countryName, countryCode),
                new Elevation(elevation),
                new Coordinate_Airport(latitude, longitude),
                area
        );

        try {
            airportRepo.save(airport);
            System.out.println("Airport created: " + iata + " / " + icao);
        } catch (ConcurrencyException | IntegrityViolationException ex) {
            System.out.println("Airport already exists: " + iata);
        }
    }
}