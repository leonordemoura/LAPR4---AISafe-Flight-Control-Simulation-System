package aiSafe.aircraftModel.repositories;

import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.domain.AircraftModelName;
import aiSafe.aircraftmanagement.domain.Maker;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AircraftModelRepository
        extends DomainRepository<AircraftModelId, AircraftModel> {

    boolean existsByNameAndMaker(AircraftModelName name, Maker maker);

    Optional<AircraftModel> findByName(AircraftModelName name);
}