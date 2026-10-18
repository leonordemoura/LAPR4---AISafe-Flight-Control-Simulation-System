package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.*;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.aircraftmanagement.domain.Country;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

@UseCaseController
public class AddAircraftController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final RepositoryFactory factory = PersistenceContext.repositories();

    private final AircraftRepository aircraftRepo = factory.aircrafts();
    private final AircraftModelRepository modelRepo = factory.aircraftModels();
    private final AirTransportCompanyRepository companyRepo = factory.getAirTransportCompanyRepository();
    private final EngineModelRepository engineModelRepo = factory.getEngineModelRepository();

    public Iterable<AircraftModel> availableModels() {
        return modelRepo.findAll();
    }

    public Iterable<AirTransportCompany> availableCompanies() {
        return companyRepo.findAll();
    }

    public Iterable<EngineModel> availableEngineModels() {
        return engineModelRepo.findAll();
    }

    public boolean registrationExists(final String registration) {
        return aircraftRepo.existsByRegistration(registration);
    }

    public Aircraft addAircraft(
            final String registration,
            final String aircraftModelId,
            final List<String> engineModelIds,
            final int firstClassSeats,
            final int businessSeats,
            final int economySeats,
            final int registrationYearValue,
            final String countryName,
            final Integer countryCode,
            final AircraftStatus status,
            final String iataCode
    ) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        TransactionalContext txCtx = factory.newTransactionalContext();

        txCtx.beginTransaction();
        try {
            AircraftRepository aircraftRepo = factory.aircrafts(txCtx);
            if (aircraftRepo.existsByRegistration(registration)) {
                throw new IllegalArgumentException("Aircraft registration already exists");
            }

            AircraftModelRepository modelRepo = factory.aircraftModels(txCtx);
            final AircraftModel model = modelRepo.ofIdentity(new AircraftModelId(aircraftModelId))
                    .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found"));

            AirTransportCompanyRepository companyRepo = factory.getAirTransportCompanyRepository(txCtx);
            final AirTransportCompany company = companyRepo.findByIataCode(IataCode.valueOf(iataCode))
                    .orElseThrow(() -> new IllegalArgumentException("Air transport company not found"));

            final AircraftRegistration regVO = AircraftRegistration.valueOf(registration);

            final List<CurrentEngine> engines = engineModelIds.stream()
                    .map(CurrentEngine::valueOf)
                    .toList();

            final CabinConfiguration cabin =
                    new CabinConfiguration(firstClassSeats, businessSeats, economySeats);

            final RegistrationYear registrationYear =
                    new RegistrationYear(registrationYearValue);

            final Country country = Country.valueOf(countryName, countryCode);

            final Aircraft aircraft = new Aircraft(
                    regVO,
                    model,
                    engines,
                    cabin,
                    registrationYear,
                    country,
                    status,
                    company
            );

            final Aircraft savedAircraft = aircraftRepo.save(aircraft);
            txCtx.commit();
            return savedAircraft;

        } catch (final Exception ex) {
            txCtx.rollback();
            throw ex;
        }
    }
}