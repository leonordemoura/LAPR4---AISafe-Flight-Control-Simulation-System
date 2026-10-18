package aiSafe.infrastructure.bootstrap;

import aiSafe.aircraftmanagement.domain.*;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;

import java.util.Optional;

public class EngineModelBootstrapper {
    private final EngineModelRepository repository = PersistenceContext.repositories().getEngineModelRepository();
    private final MakerRepository makerRepository = PersistenceContext.repositories().getMakerRepository();
    private final FuelTypeRepository fuelTypeRepository = PersistenceContext.repositories().getFuelTypeRepository();

    public void execute() {
        registerIfMissing("CFM56-7B", 27000, Unit.HORSEPOWER, 1.6e-4, "JET A-1", MotorizationType.TURBOFAN, 123L);
    }

    private void registerIfMissing(
            final String modelName,
            final Integer powerValue,
            final Unit powerUnit,
            final Double tsfcNewtonPerNewtonSecond,
            final String fuelType,
            final MotorizationType motorizationType,
            final Long makerId
    ) {
        final MakerID makerID = MakerID.valueOf(makerId);
        Optional<Maker> maker = makerRepository.findByID(makerID);
        if (maker.isEmpty()) {
            System.out.println("Maker with ID " + makerId.intValue() + " doesn't exist. Skipping.");
            return;
        }

        final FuelType fuelTypeEntity = fuelTypeRepository.findByType(FuelTypeName.valueOf(fuelType));
        if (fuelTypeEntity == null) {
            System.out.println("Fuel type " + fuelType + " doesn't exist. Skipping.");
            return;
        }

        if (repository.findByModelNameAndMaker(EngineModelName.valueOf(modelName), maker.orElse(null)).isPresent()) {
            System.out.println("EngineModel " + modelName + " for Maker ID " + makerId.intValue() + " already exists. Skipping.");
            return;
        }

        final EngineModel engineModel = new EngineModel(
                EngineModelName.valueOf(modelName),
                Power.valueOf(powerValue, powerUnit),
                TSFC.valueOf(tsfcNewtonPerNewtonSecond),
                fuelTypeEntity,
                motorizationType,
                maker.get()
        );

        try {
            repository.save(engineModel);
            System.out.println("EngineModel created: " + modelName + " for Maker ID " + makerId.intValue());
        } catch (Exception ex) {
            System.out.println("Error creating EngineModel " + modelName );

        }
    }
}
