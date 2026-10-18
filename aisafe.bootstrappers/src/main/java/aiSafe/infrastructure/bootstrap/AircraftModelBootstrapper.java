package aiSafe.infrastructure.bootstrap;

import aiSafe.aircraftModel.domain.*;
import aiSafe.aircraftmanagement.domain.EngineModelName;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

import java.util.List;
import java.util.UUID;

public class AircraftModelBootstrapper {

    public void execute() {

        registerIfMissing(
                "A320-001",
                "A320",
                123L,
                List.of("CFM56-7B"),
                AircraftType.PASSENGER,
                180
        );
    }

    private void registerIfMissing(
            final String id,
            final String name,
            final Long makerId,
            final List<String> engineNames,
            final AircraftType type,
            final int maxSeatsValue
    ) {

        final var factory = PersistenceContext.repositories();

        final var aircraftRepo = factory.aircraftModels();
        final var makerRepo = factory.getMakerRepository();
        final var engineRepo = factory.getEngineModelRepository();

        final var maker = makerRepo.findByID(
                new aiSafe.aircraftmanagement.domain.MakerID(makerId)
        ).orElseThrow(() -> new IllegalStateException("Maker not found: " + makerId));

        final var nameVO = new AircraftModelName(name);

        final var maximumSeats = new MaximumSeats(maxSeatsValue);

        if (aircraftRepo.existsByNameAndMaker(nameVO, maker)) {
            System.out.println("AircraftModel already exists: " + name);
            return;
        }

        final var engines = engineNames.stream()
                .map(nameEngine -> engineRepo.findByModelNameAndMaker(
                                new EngineModelName(nameEngine),
                                maker
                        )
                        .orElseThrow(() -> new IllegalStateException("Engine not found: " + nameEngine)))
                .toList();

        if (engines.isEmpty()) {
            throw new IllegalStateException("At least one engine required");
        }

        final var firstType = engines.get(0).engineType();

        if (engines.stream().anyMatch(e -> e.engineType() != firstType)) {
            throw new IllegalStateException("All engines must have same motorization type");
        }

        final var specs = new PhysicalSpecs(
                new Altitude(11000, AltitudeUnit.METERS),
                new Speed(828, SpeedUnit.KMH),
                new Area(122, AreaUnit.m2),
                new Weight(42000, WeightUnit.KG),
                new Weight(79000, WeightUnit.KG),
                new Weight(65000, WeightUnit.KG),
                new Weight(26000, WeightUnit.KG),
                new LiftCoefficient(1.2),
                new DragCoefficient(0.03)
        );

        final var aircraftModel = new AircraftModel(
                new AircraftModelId(id),
                nameVO,
                maker,
                engines,
                specs,
                type,
                maximumSeats
        );

        try {
            aircraftRepo.save(aircraftModel);
            System.out.println("AircraftModel created: " + name);
        } catch (ConcurrencyException | IntegrityViolationException ex) {
            System.out.println("AircraftModel already exists: " + name);
        }
    }
}