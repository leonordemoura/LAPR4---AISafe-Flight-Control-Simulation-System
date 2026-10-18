package aiSafe.airtransportcompany.application;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.CompanyName;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.domain.IcaoCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers {@link RegisterAirTransportCompanyController} orchestration (AAA, mocked collaborators).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterAirTransportCompanyControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AirTransportCompanyRepository airTransportCompanyRepository;

    private AuthorizationGuardService authorizationGuardService;

    private RegisterAirTransportCompanyController controller;

    @BeforeEach
    void setUp() {
        authorizationGuardService = new AuthorizationGuardService(authz);
        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);
        when(repositoryFactory.getAirTransportCompanyRepository(txCtx)).thenReturn(airTransportCompanyRepository);
        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);
        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();
        controller = new RegisterAirTransportCompanyController(authorizationGuardService, repositoryFactory);
    }

    @Test
    void registerAirTransportCompany_returns_true_when_persisted() {
        final CompanyName name = CompanyName.valueOf("TAP Air");
        final IataCode iata = IataCode.valueOf("TP");
        final IcaoCode icao = IcaoCode.valueOf("TAP");

        when(airTransportCompanyRepository.findByCompanyName(name)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.findByIataCode(iata)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.findByIcaoCode(icao)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.save(any(AirTransportCompany.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(controller.registerAirTransportCompany(name, iata, icao));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR);
        verify(airTransportCompanyRepository).save(any(AirTransportCompany.class));
    }

    @Test
    void registerAirTransportCompany_throws_when_company_name_exists() {
        final CompanyName name = CompanyName.valueOf("Dup");
        final IataCode iata = IataCode.valueOf("DU");
        final IcaoCode icao = IcaoCode.valueOf("DUP");

        when(airTransportCompanyRepository.findByCompanyName(name))
                .thenReturn(Optional.of(new AirTransportCompany(name, IataCode.valueOf("XX"), IcaoCode.valueOf("XXX"))));

        assertThrows(IllegalStateException.class, () -> controller.registerAirTransportCompany(name, iata, icao));
    }

    @Test
    void registerAirTransportCompany_throws_when_iata_exists() {
        final CompanyName name = CompanyName.valueOf("New Air");
        final IataCode iata = IataCode.valueOf("NA");
        final IcaoCode icao = IcaoCode.valueOf("NEW");

        when(airTransportCompanyRepository.findByCompanyName(name)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.findByIataCode(iata))
                .thenReturn(Optional.of(new AirTransportCompany(CompanyName.valueOf("Other"), iata, IcaoCode.valueOf("OTH"))));

        assertThrows(IllegalStateException.class, () -> controller.registerAirTransportCompany(name, iata, icao));
    }

    @Test
    void registerAirTransportCompany_throws_when_icao_exists() {
        final CompanyName name = CompanyName.valueOf("New Air");
        final IataCode iata = IataCode.valueOf("NA");
        final IcaoCode icao = IcaoCode.valueOf("NEW");

        when(airTransportCompanyRepository.findByCompanyName(name)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.findByIataCode(iata)).thenReturn(Optional.empty());
        when(airTransportCompanyRepository.findByIcaoCode(icao))
                .thenReturn(Optional.of(new AirTransportCompany(CompanyName.valueOf("Other"), IataCode.valueOf("OT"), icao)));

        assertThrows(IllegalStateException.class, () -> controller.registerAirTransportCompany(name, iata, icao));
    }

    @Test
    void package_constructor_throws_when_null_dependencies() {
        assertThrows(NullPointerException.class,
                () -> new RegisterAirTransportCompanyController(null, repositoryFactory));
        assertThrows(NullPointerException.class,
                () -> new RegisterAirTransportCompanyController(new AuthorizationGuardService(authz), null));
    }

    @Test
    void default_constructor_wires_persistence_and_authz_registry() {
        try (MockedStatic<AuthzRegistry> reg = mockStatic(AuthzRegistry.class);
             MockedStatic<PersistenceContext> pc = mockStatic(PersistenceContext.class)) {
            reg.when(AuthzRegistry::authorizationService).thenReturn(authz);
            doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(
                    AisafeRoles.BACKOFFICE_OPERATOR);
            pc.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

            assertDoesNotThrow(() -> {
                new RegisterAirTransportCompanyController();
            });
        }
    }
}
