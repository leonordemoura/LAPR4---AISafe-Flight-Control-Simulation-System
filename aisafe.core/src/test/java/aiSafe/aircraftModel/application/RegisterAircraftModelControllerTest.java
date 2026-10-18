package aiSafe.aircraftModel.application;

import aiSafe.aircraftModel.domain.*;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.MakerID;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import aiSafe.aircraftModel.repositories.AircraftModelRepository;
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
class RegisterAircraftModelControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private AircraftModelRepository aircraftRepo;

    @Mock
    private EngineModelRepository engineRepo;

    @Mock
    private MakerRepository makerRepo;

    @Mock
    private Maker maker;

    @Mock
    private EngineModel engine1;

    @Mock
    private EngineModel engine2;

    private RegisterAircraftModelController controller;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {

        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(authz);

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.newTransactionalContext())
                .thenReturn(txCtx);

        when(repositoryFactory.aircraftModels(txCtx))
                .thenReturn(aircraftRepo);

        when(repositoryFactory.getEngineModelRepository(txCtx))
                .thenReturn(engineRepo);

        when(repositoryFactory.getMakerRepository(txCtx))
                .thenReturn(makerRepo);

        doNothing().when(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();

        controller = new RegisterAircraftModelController();

    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private PhysicalSpecs validSpecs() {
        return new PhysicalSpecs(
                new Altitude(12000, AltitudeUnit.METERS),
                new Speed(850, SpeedUnit.KMH),
                new Area(122.6, AreaUnit.m2),
                new Weight(41000, WeightUnit.KG),
                new Weight(75000, WeightUnit.KG),
                new Weight(60000, WeightUnit.KG),
                new Weight(20000, WeightUnit.KG),
                new LiftCoefficient(1.5),
                new DragCoefficient(0.03)
        );
    }

    private MaximumSeats validSeats() {
        return new MaximumSeats(180);
    }

    // =========================================================
    // TESTS
    // =========================================================

    @Test
    void ensureAircraftModelIdCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModelId(null));
    }

    @Test
    void ensureAircraftModelNameCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModelName(""));
    }

    @Test
    void ensureMaximumSeatsMustBePositive() {

        assertThrows(IllegalArgumentException.class, () ->
                new MaximumSeats(0));
    }

    @Test
    void ensureValidPhysicalSpecsIsCreatedSuccessfully() {

        final var specs = validSpecs();

        assertNotNull(specs);
    }

    @Test
    void ensureAircraftTypeCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () ->

                new AircraftModel(
                        new AircraftModelId("A320"),
                        new AircraftModelName("Airbus A320"),
                        null,
                        List.of(),
                        validSpecs(),
                        null,
                        validSeats()
                ));
    }

    @Test
    void ensureRegisterAircraftModelRejectsEmptyEngineList() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAircraftModel(
                        "A320",
                        "Airbus A320",
                        1L,
                        List.of(),
                        validSpecs(),
                        AircraftType.PASSENGER,
                        validSeats()
                ));
    }

    @Test
    void ensureRegisterAircraftModelThrowsWhenMakerNotFound() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAircraftModel(
                        "A320",
                        "Airbus A320",
                        1L,
                        List.of(1L),
                        validSpecs(),
                        AircraftType.PASSENGER,
                        validSeats()
                ));
    }

    @Test
    void ensureRegisterAircraftModelRejectsMixedEngineTypes() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        when(engineRepo.ofIdentity(1L))
                .thenReturn(Optional.of(engine1));

        when(engineRepo.ofIdentity(2L))
                .thenReturn(Optional.of(engine2));

        when(engine1.engineType()).thenReturn(aiSafe.aircraftmanagement.domain.MotorizationType.TURBOFAN);

        when(engine2.engineType()).thenReturn(aiSafe.aircraftmanagement.domain.MotorizationType.TURBOPROP);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAircraftModel(
                        "A320",
                        "Airbus A320",
                        1L,
                        List.of(1L, 2L),
                        validSpecs(),
                        AircraftType.PASSENGER,
                        validSeats()
                ));
    }

    @Test
    void ensureRegisterAircraftModelSucceedsWithValidData() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        when(aircraftRepo.existsByNameAndMaker(any(), any()))
                .thenReturn(false);

        when(engineRepo.ofIdentity(1L))
                .thenReturn(Optional.of(engine1));

        when(engine1.engineType())
                .thenReturn(aiSafe.aircraftmanagement.domain.MotorizationType.TURBOFAN);

        final var expected = mock(AircraftModel.class);

        when(aircraftRepo.save(any(AircraftModel.class)))
                .thenReturn(expected);

        final var result = controller.registerAircraftModel(
                "A320",
                "Airbus A320",
                1L,
                List.of(1L),
                validSpecs(),
                AircraftType.PASSENGER,
                validSeats()
        );

        assertNotNull(result);

        verify(aircraftRepo).save(any(AircraftModel.class));
    }

    @Test
    void ensureAircraftModelIdToStringWorks() {

        final var id = new AircraftModelId("A320");

        assertEquals("A320", id.toString());
    }

    @Test
    void ensureAircraftModelNameToStringWorks() {

        final var name = new AircraftModelName("Airbus A320");

        assertEquals("Airbus A320", name.toString());
    }

    @Test
    void ensureEqualMaximumSeatsHaveSameHashCode() {

        final var s1 = new MaximumSeats(180);
        final var s2 = new MaximumSeats(180);

        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void ensureDifferentAircraftModelNamesAreNotEqual() {

        final var n1 = new AircraftModelName("A320");
        final var n2 = new AircraftModelName("B737");

        assertNotEquals(n1, n2);
    }

    @Test
    void ensureRegisterAircraftModelRejectsDuplicateModelForMaker() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        when(aircraftRepo.existsByNameAndMaker(any(), any()))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAircraftModel(
                        "A320",
                        "Airbus A320",
                        1L,
                        List.of(1L),
                        validSpecs(),
                        AircraftType.PASSENGER,
                        validSeats()
                ));
    }

    @Test
    void ensureRegisterAircraftModelThrowsWhenEngineNotFound() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        when(aircraftRepo.existsByNameAndMaker(any(), any()))
                .thenReturn(false);

        when(engineRepo.ofIdentity(1L))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->

                controller.registerAircraftModel(
                        "A320",
                        "Airbus A320",
                        1L,
                        List.of(1L),
                        validSpecs(),
                        AircraftType.PASSENGER,
                        validSeats()
                ));
    }

    @Test
    void ensureAllMakersReturnsRepositoryValues() {

        final List<Maker> makers = List.of(maker);

        when(makerRepo.findAll()).thenReturn(makers);

        final Iterable<Maker> result = controller.allMakers();

        assertEquals(makers, result);

        verify(makerRepo).findAll();
    }

    @Test
    void ensureAllEngineModelsReturnsRepositoryValues() {

        final List<EngineModel> engines = List.of(engine1, engine2);

        when(engineRepo.findAll()).thenReturn(engines);

        final Iterable<EngineModel> result = controller.allEngineModels();

        assertEquals(engines, result);

        verify(engineRepo).findAll();
    }

    @Test
    void ensureRegisterAircraftModelChecksAuthorization() {

        when(makerRepo.findByID(any(MakerID.class)))
                .thenReturn(Optional.of(maker));

        when(aircraftRepo.existsByNameAndMaker(any(), any()))
                .thenReturn(false);

        when(engineRepo.ofIdentity(1L))
                .thenReturn(Optional.of(engine1));

        when(engine1.engineType())
                .thenReturn(aiSafe.aircraftmanagement.domain.MotorizationType.TURBOFAN);

        when(aircraftRepo.save(any(AircraftModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        controller.registerAircraftModel(
                "A320",
                "Airbus A320",
                1L,
                List.of(1L),
                validSpecs(),
                AircraftType.PASSENGER,
                validSeats()
        );

        verify(authz)
                .ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);
    }

    @Test
    void ensureDefaultConstructorLoadsSuccessfully() {

        assertDoesNotThrow(() ->
                new RegisterAircraftModelController());
    }
}