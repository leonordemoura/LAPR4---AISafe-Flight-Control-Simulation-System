package aiSafe.infrastructure.bootstrap;

import aiSafe.aircraftmanagement.domain.FuelType;
import aiSafe.aircraftmanagement.domain.FuelTypeDensity;
import aiSafe.aircraftmanagement.domain.FuelTypeName;
import aiSafe.aircraftmanagement.domain.FuelTypeSpecificEnergy;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;

public class FuelTypeBootstrapper {
    private final FuelTypeRepository repository = PersistenceContext.repositories().getFuelTypeRepository();

    public void execute(){
        registerIfMissing( "Jet A-1", 0.840, 42.8);
        registerIfMissing( "Avgas 100LL", 0.72, 43.5);
        registerIfMissing("Sustainable Aviation Fuel (SAF)", 0.78, 43.0);
    }

    private void registerIfMissing(
            final String fuelTypeName,
            final Double density,
            final Double specificEnergy
    ) {
        if (repository.findByType(FuelTypeName.valueOf(fuelTypeName)) != null){
            System.out.println("A fuel type with that name already exists, use another name!");
            return;
        }

        try{
            FuelType fuelType = FuelType.valueOf(FuelTypeName.valueOf(fuelTypeName), FuelTypeDensity.valueOf(density), FuelTypeSpecificEnergy.valueOf(specificEnergy));
            repository.save(fuelType);
            System.out.println("FuelType created: " + fuelTypeName);
        } catch(ConcurrencyException | IntegrityViolationException ex){
            System.out.println("FuelType " + fuelTypeName + " already exists. Skipping.");
        }
    }
}
