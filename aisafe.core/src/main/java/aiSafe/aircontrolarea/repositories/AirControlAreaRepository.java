package aiSafe.aircontrolarea.repositories;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.DomainRepository;

import java.util.Optional;

public interface AirControlAreaRepository extends DomainRepository<Long, AirControlArea> {
    boolean existsByAreaCode(AreaCode areaCode);

    boolean existsByAreaName(AreaName areaName);

    Optional<AirControlArea> findByAreaCode(AreaCode areaCode);

    Iterable<AirControlArea> getAirControlAreasBasedOnUser(Email userEmail);
}
