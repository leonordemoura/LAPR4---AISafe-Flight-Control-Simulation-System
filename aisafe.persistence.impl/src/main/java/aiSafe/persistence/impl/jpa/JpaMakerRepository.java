package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.MakerName;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaMakerRepository extends JpaAutoTxRepository<Maker, MakerID, MakerID> implements MakerRepository {

    public JpaMakerRepository(final TransactionalContext autoTx) {
        super(autoTx, "makerID");
    }

    public JpaMakerRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "makerID");
    }

    @Override
    public Optional<Maker> findByID(final MakerID makerID) {
        final Optional<Maker> maker = ofIdentity(makerID);
        return maker;
    }

    @Override
    public Optional<Maker> findByName(final MakerName name) {
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name.name());
        return matchOne("lower(e.name.name) = lower(:name)", params);
    }
}
