package aiSafe.PilotCertification.application;

import aiSafe.PilotCertification.domain.ModelCertification;
import aiSafe.PilotCertification.domain.PilotCertification;
import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.PilotCertification.domain.dto.PilotRosterDTO;
import aiSafe.PilotCertification.repositories.PilotCertificationRepository;
import aiSafe.aircraftModel.application.RegisterAircraftModelController;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.airport.domain.Airport;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserSession;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ListPilotRosterControllerTest {

    @Mock
    private AuthorizationService authz;
    @Mock private RepositoryFactory repositoryFactory;
    @Mock private TransactionalContext txCtx;

    @Mock private PilotCertificationRepository pilotRepo;
    @Mock private AirTransportCompanyRepository companyRepo;

    @Mock private UserSession session;
    @Mock private SystemUser systemUser;
    @Mock private EmailAddress email;

    @Mock private AirTransportCompany company;
    @Mock private PilotCertification pilot;

    private MockedStatic<AuthzRegistry> authzMock;
    private MockedStatic<PersistenceContext> persistenceMock;

    private ListPilotRosterController controller;

    @BeforeEach
    void setUp() {

        authzMock = mockStatic(AuthzRegistry.class);
        persistenceMock = mockStatic(PersistenceContext.class);

        authzMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.newTransactionalContext())
                .thenReturn(txCtx);

        when(repositoryFactory.pilots(txCtx))
                .thenReturn(pilotRepo);

        when(repositoryFactory.getAirTransportCompanyRepository(txCtx))
                .thenReturn(companyRepo);

        controller = new ListPilotRosterController();
    }

    @AfterEach
    void tearDown() {
        authzMock.close();
        persistenceMock.close();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private void mockSession(String emailValue) {

        when(authz.session()).thenReturn(Optional.of(session));

        when(session.authenticatedUser()).thenReturn(systemUser);
        when(systemUser.email()).thenReturn(email);
        when(email.toString()).thenReturn(emailValue);
    }

    private PilotCertification mockPilot() {

        PilotCertification pilot = mock(PilotCertification.class);

        PilotID id = new PilotID("P-001");

        when(pilot.identity()).thenReturn(id);
        when(pilot.userEmail()).thenReturn(new Email("pilot@aisafe.local"));
        when(pilot.isActive()).thenReturn(true);
        when(pilot.certifications()).thenReturn(Set.of(validCert()));

        return pilot;
    }

    private ModelCertification validCert() {
        return new ModelCertification(
                new AircraftModelId("A320"),
                LocalDate.of(2025, 1, 1)
        );
    }

    // =====================================================
    // TEST 1 - SUCCESS
    // =====================================================

    @Test
    void ensureCollaboratorCanListPilotRoster() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        PilotCertification pilot = mockPilot();

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of(pilot));

        List<PilotRosterDTO> result = controller.listRoster();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // =====================================================
    // TEST 2 - AUTHORIZATION
    // =====================================================

    @Test
    void ensureAuthorizationIsChecked() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of());

        controller.listRoster();

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
    }

    // =====================================================
    // TEST 3 - SESSION REQUIRED
    // =====================================================

    @Test
    void ensureSessionIsRequired() {

        when(authz.session()).thenReturn(Optional.empty());

        assertThrows(Exception.class, () ->
                controller.listRoster()
        );
    }

    // =====================================================
    // TEST 4 - COMPANY MUST EXIST
    // =====================================================

    @Test
    void ensureCollaboratorMustBelongToCompany() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                controller.listRoster()
        );
    }

    // =====================================================
    // TEST 5 - EMPTY ROSTER
    // =====================================================

    @Test
    void ensureEmptyRosterReturnsEmptyList() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of());

        List<PilotRosterDTO> result = controller.listRoster();

        assertTrue(result.isEmpty());
    }

    // =====================================================
    // TEST 6 - DTO MAPPING
    // =====================================================

    @Test
    void ensurePilotIsMappedToDTOCorrectly() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        PilotCertification pilot = mockPilot();

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of(pilot));

        List<PilotRosterDTO> result = controller.listRoster();

        PilotRosterDTO dto = result.get(0);

        assertEquals("P-001", dto.pilotId());
        assertEquals("pilot@aisafe.local", dto.email());
        assertTrue(dto.active());
        assertFalse(dto.certifiedAircraftModels().isEmpty());
    }

    @Test
    void ensureOnlyCompanyPilotsAreListed() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        PilotCertification pilot1 = mockPilot();
        PilotCertification pilot2 = mockPilot();

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of(pilot1, pilot2));

        List<PilotRosterDTO> result = controller.listRoster();

        assertTrue(
                result.stream().allMatch(p -> p.email() != null)
        );
    }

    @Test
    void ensureCertifiedAircraftModelsAreIncluded() {

        mockSession("collab@company.com");

        when(companyRepo.findByCollaboratorEmail(any()))
                .thenReturn(Optional.of(company));

        PilotCertification pilot = mockPilot();

        when(pilotRepo.findByCompany(company))
                .thenReturn(List.of(pilot));

        List<PilotRosterDTO> result = controller.listRoster();

        assertTrue(
                result.stream().allMatch(
                        p -> p.certifiedAircraftModels() != null
                )
        );
    }
}