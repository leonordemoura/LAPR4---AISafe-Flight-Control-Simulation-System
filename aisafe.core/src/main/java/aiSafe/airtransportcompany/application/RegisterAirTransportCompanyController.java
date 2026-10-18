package aiSafe.airtransportcompany.application;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.Objects;

@UseCaseController
public class RegisterAirTransportCompanyController {
    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory repositoryFactory;

    public RegisterAirTransportCompanyController() {
        this(new AuthorizationGuardService(), PersistenceContext.repositories());
    }

    RegisterAirTransportCompanyController(
            final AuthorizationGuardService authorizationGuardService,
            final RepositoryFactory repositoryFactory
    ) {
        this.authorizationGuardService = Objects.requireNonNull(authorizationGuardService);
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
    }

    public boolean registerAirTransportCompany(final CompanyName name, final IataCode iataCode, final IcaoCode icaoCode){

        authorizationGuardService.ensureAuthenticatedBackofficeUser();

        final TransactionalContext txCtx = repositoryFactory.newTransactionalContext();

        txCtx.beginTransaction();

        try {
            final var repository = repositoryFactory.getAirTransportCompanyRepository(txCtx);

            final var existingCompany = repository.findByCompanyName(name);
            if(existingCompany.isPresent()){
                throw new IllegalStateException("An air transport company with the same name already exists.");
            }

            final var existingCompany2 = repository.findByIataCode(iataCode);
            if(existingCompany2.isPresent()){
                throw new IllegalStateException("An air transport company with the same IATA code already exists.");
            }

            final var existingCompany3 = repository.findByIcaoCode(icaoCode);
            if(existingCompany3.isPresent()){
                throw new IllegalStateException("An air transport company with the same ICAO code already exists.");
            }

            final var newCompany = new AirTransportCompany(name, iataCode, icaoCode);

            AirTransportCompany saved = repository.save(newCompany);

            System.out.printf("Air transport company '%s' with IATA code '%s' and ICAO code %s' registered successfully.%n", saved.companyName().name(), saved.iataCode().code(), saved.icaoCode().code());

            txCtx.commit();
            return true;
        } catch (final Exception ex) {
            txCtx.rollback();
            throw ex;
        }
    }

}
