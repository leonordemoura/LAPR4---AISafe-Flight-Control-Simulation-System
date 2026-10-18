package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.domain.AreaName;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaAirControlAreaRepository extends JpaAutoTxRepository<AirControlArea, Long, Long>
        implements AirControlAreaRepository {

    public JpaAirControlAreaRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaAirControlAreaRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public boolean existsByAreaCode(final AreaCode areaCode) {
        for (var area : findAll()) {
            if (area.areaCode().equals(areaCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsByAreaName(final AreaName areaName) {
        for (var area : findAll()) {
            if (area.areaName().equals(areaName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<AirControlArea> findByAreaCode(final AreaCode areaCode) {
        final Map<String, Object> params = new HashMap<>();
        params.put("areaCode", areaCode.value());
        return matchOne(
                "e.areaCode.value = :areaCode",
                params
        );
    }

    @Override
    public Iterable<AirControlArea> getAirControlAreasBasedOnUser(final Email userEmail) {
        return entityManager().createQuery(
                        """
                        SELECT DISTINCT a FROM AirControlArea a
                        JOIN a.memberships m
                        WHERE m.memberEmail = :email
                          AND m.active = true
                        """,
                        AirControlArea.class)
                .setParameter("email", userEmail)
                .getResultList();
    }
}
