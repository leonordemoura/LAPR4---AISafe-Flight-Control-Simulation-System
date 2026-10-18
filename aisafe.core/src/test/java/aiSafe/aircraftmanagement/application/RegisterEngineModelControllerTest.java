package aiSafe.aircraftmanagement.application;

import aiSafe.aircraftmanagement.domain.*;
import aiSafe.aircraftmanagement.repositories.EngineModelRepository;
import aiSafe.aircraftmanagement.repositories.FuelTypeRepository;
import aiSafe.aircraftmanagement.repositories.MakerRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class RegisterEngineModelControllerTest {

    @Mock
    private TransactionalContext txCtx;

    @Mock
    private AuthorizationService authz;
    @Mock
    private RepositoryFactory repositoryFactory;
    @Mock
    private MakerRepository makerRepository;
    @Mock
    private FuelTypeRepository fuelTypeRepository;
    @Mock
    private EngineModelRepository engineModelRepository;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    private RegisterEngineModelController controller;

    private Maker maker;
    private FuelType fuelType;
    private EngineModelName modelName;
    private Power power;
    private TSFC tsfc;
    private FuelTypeName fuelTypeName;

    @BeforeEach
    void setUp() {
        authzRegistryMock = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        when(repositoryFactory.getFuelTypeRepository()).thenReturn(fuelTypeRepository);

        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);

        authzRegistryMock.when(AuthzRegistry::authorizationService).thenReturn(authz);
        persistenceContextMock.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

        when(repositoryFactory.getMakerRepository(txCtx)).thenReturn(makerRepository);
        when(repositoryFactory.getFuelTypeRepository(txCtx)).thenReturn(fuelTypeRepository);
        when(repositoryFactory.getEngineModelRepository(txCtx)).thenReturn(engineModelRepository);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();

        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR);

        controller = new RegisterEngineModelController();

        maker = new Maker(MakerID.valueOf(1L), MakerName.valueOf("MakerCo"), Country.valueOf("PT", 620));
        fuelType = new FuelType(FuelTypeName.valueOf("JET A"), FuelTypeDensity.valueOf(0.8), FuelTypeSpecificEnergy.valueOf(43.0));
        modelName = EngineModelName.valueOf("CFM56");
        power = Power.valueOf(1000, Unit.KILOWATT);
        tsfc = TSFC.valueOf(0.1);
        fuelTypeName = FuelTypeName.valueOf("JET A");
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    @Test
    void registerEngineModel_saves_when_maker_and_fuel_exist_and_name_unique_for_maker() {
        // Arrange
        when(makerRepository.findByID(any(MakerID.class))).thenReturn(Optional.of(maker));
        when(fuelTypeRepository.findByType(any(FuelTypeName.class))).thenReturn(fuelType);
        when(engineModelRepository.findByModelNameAndMaker(any(EngineModelName.class), any(Maker.class)))
                .thenReturn(Optional.empty());

        final EngineModel saved = mock(EngineModel.class);
        when(saved.identity()).thenReturn(99L);
        when(engineModelRepository.save(any(EngineModel.class))).thenReturn(saved);

        // Act
        final boolean result = controller.registerEngineModel(
                modelName, power, tsfc, fuelTypeName, MotorizationType.TURBOFAN, 1L);

        // Assert
        assertTrue(result);
        verify(engineModelRepository).save(any(EngineModel.class));
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR);
    }

    @Test
    void registerEngineModel_throws_when_maker_unknown() {
        // Arrange
        when(makerRepository.findByID(any(MakerID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerEngineModel(
                        modelName, power, tsfc, fuelTypeName, MotorizationType.TURBOFAN, 999L));
    }

    @Test
    void registerEngineModel_throws_when_fuel_type_unknown() {
        // Arrange
        when(makerRepository.findByID(any(MakerID.class))).thenReturn(Optional.of(maker));
        when(fuelTypeRepository.findByType(any(FuelTypeName.class))).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerEngineModel(
                        modelName, power, tsfc, fuelTypeName, MotorizationType.TURBOFAN, 1L));
    }

    @Test
    void registerEngineModel_throws_when_duplicate_name_for_same_maker() {
        // Arrange
        when(makerRepository.findByID(any(MakerID.class))).thenReturn(Optional.of(maker));
        when(fuelTypeRepository.findByType(any(FuelTypeName.class))).thenReturn(fuelType);
        final EngineModel existing = mock(EngineModel.class);
        when(engineModelRepository.findByModelNameAndMaker(any(EngineModelName.class), any(Maker.class)))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> controller.registerEngineModel(
                        modelName, power, tsfc, fuelTypeName, MotorizationType.TURBOFAN, 1L));
        verify(engineModelRepository, never()).save(any());
    }

    @Test
    void getFuelTypes_returns_repository_list() {
        // Arrange
        final List<FuelType> expected = List.of(fuelType);
        when(fuelTypeRepository.getAllFuelTypes()).thenReturn(expected);

        // Act
        final List<FuelType> result = controller.getFuelTypes();

        // Assert
        assertEquals(expected, result);
        verify(fuelTypeRepository).getAllFuelTypes();
        verify(authz).ensureAuthenticatedUserHasAnyOf(AisafeRoles.ADMIN, AisafeRoles.BACKOFFICE_OPERATOR);
    }
}
