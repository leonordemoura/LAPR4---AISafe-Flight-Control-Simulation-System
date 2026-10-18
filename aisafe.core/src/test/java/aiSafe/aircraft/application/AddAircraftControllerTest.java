package aiSafe.aircraft.application;

import aiSafe.aircraft.domain.*;
import aiSafe.aircraft.repositories.AircraftRepository;
import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftModel.domain.AircraftModelId;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.airtransportcompany.repositories.AirTransportCompanyRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddAircraftControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AircraftRepository aircraftRepo;

    @Mock
    private AircraftModelRepository modelRepo;

    @Mock
    private AirTransportCompanyRepository companyRepo;

    @Mock
    private AircraftModel aircraftModel;

    @Mock
    private AirTransportCompany company;

    @Mock
    private Aircraft aircraft;

    @Mock
    private aiSafe.aircraftModel.domain.MaximumSeats maximumSeats;

    private AddAircraftController controller;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    // =========================================================
    // SETUP / TEARDOWN
    // =========================================================

    @BeforeEach
    void setUp() {
        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.aircrafts()).thenReturn(aircraftRepo);
        when(repositoryFactory.aircraftModels()).thenReturn(modelRepo);
        when(repositoryFactory.getAirTransportCompanyRepository()).thenReturn(companyRepo);

        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);

        when(repositoryFactory.aircrafts(txCtx)).thenReturn(aircraftRepo);
        when(repositoryFactory.aircraftModels(txCtx)).thenReturn(modelRepo);
        when(repositoryFactory.getAirTransportCompanyRepository(txCtx)).thenReturn(companyRepo);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();


        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);

        controller = new AddAircraftController();
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private List<String> validEngineIds() {
        return List.of("ENG-001", "ENG-002");
    }

    /** Stubs the mock AircraftModel so the Aircraft constructor doesn't NPE on maximumSeats(). */
    private void stubAircraftModel() {
        when(maximumSeats.seats()).thenReturn(180);
        when(aircraftModel.maximumSeats()).thenReturn(maximumSeats);
    }

    private Aircraft callAddAircraft(final String registration) {
        return controller.addAircraft(
                registration,
                "A320",
                validEngineIds(),
                8,
                24,
                148,
                2018,
                "Portugal",
                351,
                AircraftStatus.ACTIVE,
                "TP"
        );
    }

    // =========================================================
    // TESTS — availableModels / availableCompanies
    // =========================================================

    @Test
    void ensureAvailableModelsReturnsRepositoryValues() {
        final List<AircraftModel> models = List.of(aircraftModel);
        when(modelRepo.findAll()).thenReturn(models);

        final Iterable<AircraftModel> result = controller.availableModels();

        assertEquals(models, result);
        verify(modelRepo).findAll();
    }

    @Test
    void ensureAvailableCompaniesReturnsRepositoryValues() {
        final List<AirTransportCompany> companies = List.of(company);
        when(companyRepo.findAll()).thenReturn(companies);

        final Iterable<AirTransportCompany> result = controller.availableCompanies();

        assertEquals(companies, result);
        verify(companyRepo).findAll();
    }

    // =========================================================
    // TESTS — registrationExists
    // =========================================================

    @Test
    void ensureRegistrationExistsReturnsTrueWhenFound() {
        when(aircraftRepo.existsByRegistration("CS-TKY")).thenReturn(true);

        assertTrue(controller.registrationExists("CS-TKY"));
    }

    @Test
    void ensureRegistrationExistsReturnsFalseWhenNotFound() {
        when(aircraftRepo.existsByRegistration("CS-NEW")).thenReturn(false);

        assertFalse(controller.registrationExists("CS-NEW"));
    }

    // =========================================================
    // TESTS — addAircraft: guard clauses
    // =========================================================

    @Test
    void ensureAddAircraftThrowsWhenRegistrationAlreadyExists() {
        when(aircraftRepo.existsByRegistration("CS-TKY")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                controller.addAircraft(
                        "CS-TKY",
                        "A320",
                        validEngineIds(),
                        8, 24, 148,
                        2018, "Portugal", 351,
                        AircraftStatus.ACTIVE,
                        "TP"
                ));
    }

    @Test
    void ensureAddAircraftThrowsWhenAircraftModelNotFound() {
        when(aircraftRepo.existsByRegistration(any())).thenReturn(false);
        when(modelRepo.ofIdentity(any(AircraftModelId.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> callAddAircraft("CS-TKY"));
    }

    @Test
    void ensureAddAircraftThrowsWhenCompanyNotFound() {
        when(aircraftRepo.existsByRegistration(any())).thenReturn(false);
        when(modelRepo.ofIdentity(any(AircraftModelId.class))).thenReturn(Optional.of(aircraftModel));
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> callAddAircraft("CS-TKY"));
    }

    // =========================================================
    // TESTS — addAircraft: success path
    // =========================================================

    @Test
    void ensureAddAircraftSucceedsWithValidData() {
        stubAircraftModel();
        when(aircraftRepo.existsByRegistration(any())).thenReturn(false);
        when(modelRepo.ofIdentity(any(AircraftModelId.class))).thenReturn(Optional.of(aircraftModel));
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.of(company));
        when(aircraftRepo.save(any(Aircraft.class))).thenAnswer(inv -> inv.getArgument(0));

        final Aircraft result = callAddAircraft("CS-TKY");

        assertNotNull(result);
        verify(aircraftRepo).save(any(Aircraft.class));
    }

    @Test
    void ensureAddAircraftPersistsExactlyOnce() {
        stubAircraftModel();
        when(aircraftRepo.existsByRegistration(any())).thenReturn(false);
        when(modelRepo.ofIdentity(any(AircraftModelId.class))).thenReturn(Optional.of(aircraftModel));
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.of(company));
        when(aircraftRepo.save(any(Aircraft.class))).thenAnswer(inv -> inv.getArgument(0));

        callAddAircraft("CS-TKY");

        verify(aircraftRepo, times(1)).save(any(Aircraft.class));
    }

    // =========================================================
    // TESTS — authorization
    // =========================================================

    @Test
    void ensureAddAircraftChecksAuthorization() {
        stubAircraftModel();
        when(aircraftRepo.existsByRegistration(any())).thenReturn(false);
        when(modelRepo.ofIdentity(any(AircraftModelId.class))).thenReturn(Optional.of(aircraftModel));
        when(companyRepo.findByIataCode(any(IataCode.class))).thenReturn(Optional.of(company));
        when(aircraftRepo.save(any(Aircraft.class))).thenAnswer(inv -> inv.getArgument(0));

        callAddAircraft("CS-TKY");

        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR, AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
    }

    // =========================================================
    // TESTS — value objects / domain rules
    // =========================================================

    @Test
    void ensureAircraftRegistrationCannotBeNull() {
        assertThrows(Exception.class, () ->
                AircraftRegistration.valueOf(null));
    }

    @Test
    void ensureAircraftRegistrationCannotBeEmpty() {
        assertThrows(Exception.class, () ->
                AircraftRegistration.valueOf(""));
    }

    @Test
    void ensureRegistrationYearCannotBeZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegistrationYear(0));
    }

    @Test
    void ensureRegistrationYearCannotBeNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegistrationYear(-1));
    }

    @Test
    void ensureCabinConfigurationCannotHaveNegativeSeats() {
        assertThrows(IllegalArgumentException.class, () ->
                new CabinConfiguration(-1, 0, 0));
    }

    @Test
    void ensureCabinConfigurationWithAllZeroSeatsIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new CabinConfiguration(0, 0, 0));
    }

    // =========================================================
    // TESTS — default constructor
    // =========================================================

    @Test
    void ensureDefaultConstructorLoadsSuccessfully() {
        assertDoesNotThrow(() -> new AddAircraftController());
    }
}