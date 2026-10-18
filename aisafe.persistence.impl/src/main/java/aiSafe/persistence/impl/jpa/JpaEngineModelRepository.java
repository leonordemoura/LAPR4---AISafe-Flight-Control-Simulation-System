package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircraftmanagement.domain.*;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JpaEngineModelRepository extends JpaAutoTxRepository<EngineModel, Long, Long> implements EngineModelRepository {

    public JpaEngineModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "engineModelId");
    }

    public JpaEngineModelRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "engineModelId");
    }

    @Override
    public Optional<EngineModel> findByModelNameAndMaker(final EngineModelName modelName, final Maker maker) {
        final Map<String, Object> params = new HashMap<>();
        params.put("modelName", modelName);
        params.put("maker", maker);
        return matchOne("e.modelName = :modelName and e.maker = :maker", params);
    }
}
