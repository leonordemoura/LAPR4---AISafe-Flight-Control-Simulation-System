package aiSafe.infrastructure.bootstrap;

import aiSafe.Application;
import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.domain.Coordinate;
import aiSafe.aircontrolarea.domain.GeoBoundary;
import aiSafe.aircontrolarea.domain.MaxAltitude;
import aiSafe.aircontrolarea.domain.MinFuelAtArrival;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

import java.util.List;

public class AirControlAreaBootstrapper {
    private static final String MAX_ALTITUDE_CONFIG_KEY = "aircontrolarea.maxAltitudeMeters";

    private final AirControlAreaRepository repository = PersistenceContext.repositories().airControlAreas();

    public void execute() {
        registerIfMissing(
                "LPPT",
                "Lisbon Control",
                100.0,
                List.of(
                        new Coordinate(38.0, -10.0),
                        new Coordinate(38.0, -8.0),
                        new Coordinate(40.0, -10.0),
                        new Coordinate(40.0, -8.0)
                )
        );
        //add new aircontrolarea for testing purposes - us052
        registerIfMissing(
                "LPPR",
                "Porto Control",
                120.0,
                List.of(
                        new Coordinate(40.5, -9.5),
                        new Coordinate(40.5, -7.5),
                        new Coordinate(42.0, -9.5),
                        new Coordinate(42.0, -7.5)
                )
        );
    }

    private void registerIfMissing(
            final String code,
            final String name,
            final double minFuel,
            final List<Coordinate> coordinates
    ) {
        final AreaCode areaCode = new AreaCode(code);
        if (repository.existsByAreaCode(areaCode)) {
            System.out.println("Air Control Area with code " + code + " already exists. Skipping.");
            return;
        }
        final GeoBoundary newBoundary = new GeoBoundary(coordinates);
        for (final AirControlArea existingArea : repository.findAll()) {
            if (existingArea.geoBoundary().overlapsWith(newBoundary)) {
                System.out.println(
                        "Air Control Area " + code + " overlaps with existing area "
                                + existingArea.areaCode().value() + ". Skipping."
                );
                return;
            }
        }

        final AirControlArea airControlArea = new AirControlArea(
                areaCode,
                new AreaName(name),
                new MinFuelAtArrival(minFuel),
                new MaxAltitude(resolveConfiguredMaxAltitudeMeters()),
                newBoundary
        );
        try{
            repository.save(airControlArea);
            System.out.println("Air Control Area created: " + areaCode.value());
        } catch(ConcurrencyException | IntegrityViolationException ex){
            System.out.println("Air Control Area with code " + code + " already exists. Skipping.");
        }
    }

    private double resolveConfiguredMaxAltitudeMeters() {
        final String rawValue = Application.settings().getProperty(MAX_ALTITUDE_CONFIG_KEY);
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalStateException("Missing configuration key: " + MAX_ALTITUDE_CONFIG_KEY);
        }
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (final NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid value for " + MAX_ALTITUDE_CONFIG_KEY + ": " + rawValue,
                    ex
            );
        }
    }
}
