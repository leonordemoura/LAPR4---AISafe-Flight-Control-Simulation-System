package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.domain.AircraftModelName;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.aircraftmanagement.domain.Maker;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import jakarta.persistence.TypedQuery;
import java.util.Optional;

public class JpaAircraftModelRepository
        extends JpaAutoTxRepository<AircraftModel, AircraftModelId, AircraftModelId>
        implements AircraftModelRepository {

    public JpaAircraftModelRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaAircraftModelRepository(final String puname) {
        super(
                puname,
                Application.settings().getExtendedPersistenceProperties(),
                "id"
        );
    }

    @Override
    public boolean existsByNameAndMaker(AircraftModelName name, Maker maker) {
        TypedQuery<Long> query = entityManager().createQuery(
                "SELECT COUNT(m) FROM AircraftModel m " +
                        "WHERE m.name = :name AND m.maker = :maker",
                Long.class
        );
        query.setParameter("name", name);
        query.setParameter("maker", maker);
        return query.getSingleResult() > 0;
    }

    @Override
    public Optional<AircraftModel> findByName(AircraftModelName name) {
        TypedQuery<AircraftModel> query = entityManager().createQuery(
                "SELECT m FROM AircraftModel m WHERE m.name = :name",
                AircraftModel.class
        );
        query.setParameter("name", name);
        return query.getResultStream().findFirst();
    }
}