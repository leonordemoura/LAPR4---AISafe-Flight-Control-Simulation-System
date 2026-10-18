package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircraftmanagement.domain.FuelType;
import aiSafe.aircraftmanagement.domain.FuelTypeName;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JpaFuelTypeRepository extends JpaAutoTxRepository<FuelType, Long, Long> implements FuelTypeRepository {

    public JpaFuelTypeRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaFuelTypeRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public List<FuelType> getAllFuelTypes() {
        final Iterable<FuelType> all = findAll();
        final List<FuelType> result = new java.util.ArrayList<>();
        all.forEach(result::add);
        return result;
    }

    @Override
    public FuelType findByType(final FuelTypeName type) {
        final Map<String, Object> params = new HashMap<>();
        params.put("type", type.type());
        return matchOne("lower(e.type.type) = lower(:type)", params)
                .orElse(null);
    }
}

