package aiSafe.aircraftmanagement.repositories;

import aiSafe.aircraftmanagement.domain.*;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface EngineModelRepository extends DomainRepository<Long, EngineModel> {

    Optional<EngineModel> findByModelNameAndMaker(EngineModelName modelName, Maker maker);
}
