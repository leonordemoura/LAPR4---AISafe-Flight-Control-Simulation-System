package aiSafe.aircraftModel.application;

import aiSafe.aircraftModel.domain.*;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

@UseCaseController
public class RegisterAircraftModelController {

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final RepositoryFactory factory =
            PersistenceContext.repositories();

    public AircraftModel registerAircraftModel(
            final String aircraftModelId,
            final String name,
            final Long makerId,
            final List<Long> engineIds,
            final PhysicalSpecs specs,
            final AircraftType type,
            final MaximumSeats maximumSeats
    ) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.BACKOFFICE_OPERATOR
        );

        final var txCtx = factory.newTransactionalContext();

        final var aircraftRepo =
                factory.aircraftModels(txCtx);

        final var engineRepo =
                factory.getEngineModelRepository(txCtx);

        final var makerRepo =
                factory.getMakerRepository(txCtx);

        txCtx.beginTransaction();

        try {

            final var maker = makerRepo.findByID(
                    new MakerID(makerId)
            ).orElseThrow(() ->
                    new IllegalArgumentException("Maker not found"));

            final var modelName =
                    new AircraftModelName(name);

            if (aircraftRepo.existsByNameAndMaker(
                    modelName,
                    maker
            )) {
                throw new IllegalArgumentException(
                        "Aircraft model already exists"
                );
            }

            if (engineIds == null || engineIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "At least one engine required"
                );
            }

            final var engines = engineIds.stream()
                    .map(id -> engineRepo.ofIdentity(id)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Engine not found: " + id)))
                    .toList();

            final var firstType =
                    engines.get(0).engineType();

            boolean sameType = engines.stream()
                    .allMatch(e ->
                            e.engineType() == firstType);

            if (!sameType) {
                throw new IllegalArgumentException(
                        "All engines must have same motorization type"
                );
            }

            final var id =
                    new AircraftModelId(aircraftModelId);

            final var aircraftModel =
                    new AircraftModel(
                            id,
                            modelName,
                            maker,
                            engines,
                            specs,
                            type,
                            maximumSeats
                    );

            final var saved =
                    aircraftRepo.save(aircraftModel);

            txCtx.commit();

            return saved;

        } catch (Exception e) {

            txCtx.rollback();
            throw e;
        }
    }

    public Iterable<Maker> allMakers() {
        final var txCtx = factory.newTransactionalContext();

        return factory.getMakerRepository(txCtx).findAll();
    }

    public Iterable<EngineModel> allEngineModels() {
        final var txCtx = factory.newTransactionalContext();

        return factory.getEngineModelRepository(txCtx).findAll();
    }

}
