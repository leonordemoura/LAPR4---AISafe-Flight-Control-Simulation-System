package aiSafe.aircraftmanagement.repositories;

import aiSafe.aircraftmanagement.domain.FuelType;
import aiSafe.aircraftmanagement.domain.FuelTypeName;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.List;

public interface FuelTypeRepository extends DomainRepository<Long, FuelType> {

    List<FuelType> getAllFuelTypes();

    FuelType findByType(FuelTypeName type);
}
