package aiSafe.aircraftmanagement.application;

import aiSafe.aircraftmanagement.domain.*;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.List;

@UseCaseController
public class RegisterEngineModelController {
    private final AuthorizationGuardService authorizationGuardService;

    public RegisterEngineModelController() {
        this.authorizationGuardService = new AuthorizationGuardService();
    }

    public boolean registerEngineModel(final EngineModelName modelName, final Power power, final TSFC tsfc, final FuelTypeName fuelTypeName, final MotorizationType engineType,  final Long makerID){

        authorizationGuardService.ensureAuthenticatedBackofficeUser();

        final TransactionalContext txCtx = PersistenceContext.repositories().newTransactionalContext();

        txCtx.beginTransaction();

        try {
            final var makerRepository = PersistenceContext.repositories().getMakerRepository(txCtx);
            final var maker = makerRepository.findByID(MakerID.valueOf(makerID))
                    .orElseThrow(() -> new IllegalArgumentException("Maker with the given ID does not exist."));

            final var fuelTypeRepository = PersistenceContext.repositories().getFuelTypeRepository(txCtx);
            final var fuelType= fuelTypeRepository.findByType(fuelTypeName);
            if (fuelType == null) {
                throw new IllegalArgumentException("Fuel type with the given name does not exist.");
            }

            final var repository = PersistenceContext.repositories().getEngineModelRepository(txCtx);
            final var existingModel = repository.findByModelNameAndMaker(modelName, maker);

            if(existingModel.isPresent()){
                throw new IllegalStateException("An engine model with the same name and maker already exists.");
            }


            final var newModel = new EngineModel(modelName, power, tsfc, fuelType, engineType, maker);

            EngineModel saved = repository.save(newModel);

            System.out.printf("Engine model registered with id %d.%n", saved.identity());

            txCtx.commit();
            return true;
        } catch (final Exception e) {
            txCtx.rollback();
            throw e;
        }
    }

    public List<FuelType> getFuelTypes() {
        authorizationGuardService.ensureAuthenticatedBackofficeUser();
        final var fuelTypeRepository = PersistenceContext.repositories().getFuelTypeRepository();
        return fuelTypeRepository.getAllFuelTypes();
    }
}
