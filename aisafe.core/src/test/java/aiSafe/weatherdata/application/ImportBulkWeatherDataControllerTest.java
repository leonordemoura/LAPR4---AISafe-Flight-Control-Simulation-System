package aiSafe.weatherdata.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.WeatherDataImporter;
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

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportBulkWeatherDataControllerTest {

    @Mock
    private AuthorizationService authz;

    @Mock
    private ImportBulkWeatherDataService service;

    @Mock
    private RepositoryFactory repositoryFactory;

    @Mock
    private WeatherDataImporter importer;

    private ImportBulkWeatherDataController controller;

    private MockedStatic<AuthzRegistry> authzRegistryMock;
    private MockedStatic<PersistenceContext> persistenceContextMock;

    @BeforeEach
    void setUp() {
        authzRegistryMock      = mockStatic(AuthzRegistry.class);
        persistenceContextMock = mockStatic(PersistenceContext.class);

        authzRegistryMock.when(AuthzRegistry::authorizationService).thenReturn(authz);
        persistenceContextMock.when(PersistenceContext::repositories).thenReturn(repositoryFactory);

        doNothing().when(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);

        when(importer.formatName()).thenReturn("CSV");
        when(importer.importFrom(any())).thenReturn(List.of());
        when(service.processRows(any())).thenReturn(List.of());

        controller = new ImportBulkWeatherDataController(authz, service,
                path -> new ByteArrayInputStream(new byte[0]));
        controller.registerImporter(importer);
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
    void ensurePublicConstructorInitializesController() {
        assertDoesNotThrow(() -> new ImportBulkWeatherDataController());
    }

    // =========================================================
    // PACKAGE-PRIVATE CONSTRUCTORS
    // =========================================================

    @Test
    void ensurePackagePrivateConstructorWithoutFileOpenerInitializes() {
        assertDoesNotThrow(() -> new ImportBulkWeatherDataController(authz, service));
    }

    // =========================================================
    // REGISTER IMPORTER
    // =========================================================

    @Test
    void ensureRegisterImporterAddsFormat() {
        assertTrue(controller.availableFormats().contains("CSV"));
    }

    @Test
    void ensureRegisterImporterThrowsWhenNull() {
        assertThrows(NullPointerException.class, () -> controller.registerImporter(null));
    }

    // =========================================================
    // AVAILABLE FORMATS
    // =========================================================

    @Test
    void ensureAvailableFormatsReturnsRegisteredFormats() {
        final List<String> formats = controller.availableFormats();

        assertNotNull(formats);
        assertEquals(1, formats.size());
        assertEquals("CSV", formats.get(0));
    }

    // =========================================================
    // IMPORT FROM FILE
    // =========================================================

    @Test
    void ensureImportFromFileSucceedsWithValidFormat() {
        final List<WeatherDataImportResult> results =
                controller.importFromFile("any/path.csv", "CSV");

        assertNotNull(results);
        verify(service).processRows(any());
    }

    @Test
    void ensureImportFromFileIsCaseInsensitiveForFormat() {
        final List<WeatherDataImportResult> results =
                controller.importFromFile("any/path.csv", "csv");

        assertNotNull(results);
        verify(service).processRows(any());
    }

    @Test
    void ensureImportFromFileThrowsWhenFormatUnknown() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.importFromFile("path.csv", "XML"));
    }

    @Test
    void ensureImportFromFileThrowsWhenFilePathIsNull() {
        assertThrows(NullPointerException.class, () ->
                controller.importFromFile(null, "CSV"));
    }

    @Test
    void ensureImportFromFileThrowsWhenFormatIsNull() {
        assertThrows(NullPointerException.class, () ->
                controller.importFromFile("path.csv", null));
    }

    @Test
    void ensureAuthorizationIsEnforced() {
        controller.importFromFile("any/path.csv", "CSV");

        verify(authz).ensureAuthenticatedUserHasAnyOf(
                AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
    }

    @Test
    void ensureImportThrowsWhenUnauthorized() {
        doThrow(new RuntimeException("Unauthorized"))
                .when(authz).ensureAuthenticatedUserHasAnyOf(
                        AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);

        assertThrows(RuntimeException.class, () ->
                controller.importFromFile("path.csv", "CSV"));
    }

    @Test
    void ensureImportFromFileThrowsWhenFileCannotBeRead() {
        final ImportBulkWeatherDataController badController =
                new ImportBulkWeatherDataController(authz, service,
                        path -> { throw new Exception("file not found"); });
        badController.registerImporter(importer);

        assertThrows(IllegalArgumentException.class, () ->
                badController.importFromFile("missing.csv", "CSV"));
    }
}