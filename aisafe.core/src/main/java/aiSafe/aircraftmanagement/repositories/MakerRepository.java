package aiSafe.aircraftmanagement.repositories;

import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.MakerName;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface MakerRepository extends DomainRepository<MakerID, Maker> {

    Optional<Maker> findByID(MakerID makerID);

    Optional<Maker> findByName(MakerName name);
}
