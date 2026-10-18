package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.weatherdata.domain.*;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.WeatherDataImportRow;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportBulkWeatherDataServiceTest {

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private RegisterWeatherDataController registerController;

    @Mock
    private AirControlAreaRepository areaRepo;

    @Mock
    private TransactionalContext txCtx;

    private ImportBulkWeatherDataService service;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {
        authzRegistryMock      = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService)
                .thenReturn(mock(AuthorizationService.class));

        persistenceContextMock.when(PersistenceContext::repositories)
                .thenReturn(repositoryFactory);

        when(repositoryFactory.newTransactionalContext()).thenReturn(txCtx);
        when(repositoryFactory.airControlAreas(txCtx)).thenReturn(areaRepo);
        when(areaRepo.existsByAreaCode(any())).thenReturn(true);

        doNothing().when(txCtx).beginTransaction();
        doNothing().when(txCtx).commit();
        doNothing().when(txCtx).rollback();
        doNothing().when(txCtx).close();

        service = new ImportBulkWeatherDataService(repositoryFactory, registerController);
    }

    @AfterEach
    void tearDown() {
        authzRegistryMock.close();
        persistenceContextMock.close();
    }

    // =========================================================
    // PUBLIC CONSTRUCTOR
    // =========================================================

    @Test
    void ensurePublicConstructorInitializesService() {
        assertDoesNotThrow(() -> new ImportBulkWeatherDataService());
    }

    // =========================================================
    // PROCESS ROWS
    // =========================================================

    @Test
    void ensureValidRowIsImportedSuccessfully() {
        final List<WeatherDataImportResult> results =
                service.processRows(List.of(buildRow("LPPT")));

        assertEquals(1, results.size());
        assertTrue(results.get(0).success());
        verify(registerController).registerWeatherData(
                any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(),
                any(), anyDouble(),
                anyDouble(), any(),
                anyDouble(), any(),
                anyDouble(), any()
        );
    }

    @Test
    void ensureEmptyRowListReturnsEmptyResults() {
        final List<WeatherDataImportResult> results = service.processRows(List.of());

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void ensureFailedRowIsRecordedWithoutStoppingImport() {
        doThrow(new IllegalArgumentException("duplicate"))
                .when(registerController).registerWeatherData(
                        any(), any(), any(),
                        anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                        anyDouble(), anyDouble(),
                        any(), anyDouble(),
                        anyDouble(), any(),
                        anyDouble(), any(),
                        anyDouble(), any()
                );

        final List<WeatherDataImportResult> results = service.processRows(List.of(
                buildRow("LPPT"),
                buildRow("LPPT")
        ));

        assertEquals(2, results.size());
        assertFalse(results.get(0).success());
        assertFalse(results.get(1).success());
        assertEquals("duplicate", results.get(0).errorMessage());
    }

    @Test
    void ensureAltitudeIsConvertedFromFeetToMeters() {
        service.processRows(List.of(buildRow("LPPT")));

        verify(registerController).registerWeatherData(
                any(), any(), any(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(),
                eq(0.0 * ImportBulkWeatherDataService.FEET_TO_METERS),
                eq(1000.0 * ImportBulkWeatherDataService.FEET_TO_METERS),
                any(), anyDouble(),
                anyDouble(), any(),
                anyDouble(), any(),
                anyDouble(), any()
        );
    }

    @Test
    void ensureLineNumberStartsAt2AndIncrements() {
        final List<WeatherDataImportResult> results = service.processRows(List.of(
                buildRow("LPPT"),
                buildRow("LPPT")
        ));

        assertEquals(2, results.get(0).lineNumber());
        assertEquals(3, results.get(1).lineNumber());
    }

    @Test
    void ensureAreaExistenceIsCheckedOncePerAreaCode() {
        service.processRows(List.of(
                buildRow("LPPT"),
                buildRow("LPPT"),
                buildRow("LPPT")
        ));

        verify(areaRepo, times(1)).existsByAreaCode(any());
    }

    @Test
    void ensureDistinctAreaCodesEachTriggerAnExistenceCheck() {
        service.processRows(List.of(
                buildRow("LPPT"),
                buildRow("EGLL")
        ));

        verify(areaRepo, times(2)).existsByAreaCode(any());
    }

    @Test
    void ensureAreaIsCreatedAndTransactionCommittedWhenItDoesNotExist() {
        when(areaRepo.existsByAreaCode(any())).thenReturn(false);
        when(areaRepo.save(any())).thenReturn(null);

        service.processRows(List.of(buildRow("LPPT")));

        verify(areaRepo).save(any());
        verify(txCtx).commit();
    }

    @Test
    void ensureTransactionIsCommittedWhenAreaAlreadyExists() {
        // existsByAreaCode devolve true por defeito no setUp — early return
        service.processRows(List.of(buildRow("LPPT")));

        verify(txCtx).commit();
    }

    @Test
    void ensureRollbackAndCloseAreCalledWhenAreaCreationFails() {
        when(areaRepo.existsByAreaCode(any())).thenReturn(false);
        when(areaRepo.save(any())).thenThrow(new RuntimeException("db error"));

        final List<WeatherDataImportResult> results =
                service.processRows(List.of(buildRow("LPPT")));

        assertFalse(results.get(0).success());
        verify(txCtx).rollback();
        verify(txCtx).close();
    }

    @Test
    void ensureFailureInBeginTransactionIsRecordedAsRowFailure() {
        doThrow(new RuntimeException("connection refused"))
                .when(txCtx).beginTransaction();

        final List<WeatherDataImportResult> results =
                service.processRows(List.of(buildRow("LPPT")));

        assertEquals(1, results.size());
        assertFalse(results.get(0).success());
        assertEquals("connection refused", results.get(0).errorMessage());
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private WeatherDataImportRow buildRow(final String areaCode) {
        return new WeatherDataImportRow(
                areaCode,
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 1, 23, 59),
                38.5, 39.5,
                -9.5, -8.5,
                0.0, 1000.0,
                "METAR",
                180.0,
                10.0, WindSpeedUnit.KT,
                15.0, TemperatureUnit.C,
                1013.25, PressureUnit.HPA
        );
    }
}