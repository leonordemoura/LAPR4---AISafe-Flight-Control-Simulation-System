package aiSafe.weatherdata.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.WeatherDataImportRow;
import aiSafe.weatherdata.importer.WeatherDataImporter;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

@UseCaseController
public class ImportBulkWeatherDataController {

    @FunctionalInterface
    public interface FileOpener {
        InputStream open(String filePath) throws Exception;
    }

    private final AuthorizationService authz;
    private final ImportBulkWeatherDataService service;
    private final FileOpener fileOpener;
    private final Map<String, WeatherDataImporter> importers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public ImportBulkWeatherDataController() {
        this.authz      = AuthzRegistry.authorizationService();
        this.service    = new ImportBulkWeatherDataService();
        this.fileOpener = FileInputStream::new;
    }

    ImportBulkWeatherDataController(final AuthorizationService authz,
                                    final ImportBulkWeatherDataService service) {
        this(authz, service, FileInputStream::new);
    }

    ImportBulkWeatherDataController(final AuthorizationService authz,
                                    final ImportBulkWeatherDataService service,
                                    final FileOpener fileOpener) {
        this.authz      = authz;
        this.service    = service;
        this.fileOpener = fileOpener;
    }

    public void registerImporter(final WeatherDataImporter importer) {
        Objects.requireNonNull(importer);
        importers.put(importer.formatName(), importer);
    }

    public List<String> availableFormats() {
        return List.copyOf(importers.keySet());
    }

    public List<WeatherDataImportResult> importFromFile(final String filePath, final String format) {
        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN);
        Objects.requireNonNull(filePath, "File path must not be null");
        Objects.requireNonNull(format,   "Format must not be null");

        final WeatherDataImporter importer = importers.get(format);
        if (importer == null) {
            throw new IllegalArgumentException("No importer registered for format: " + format);
        }

        final List<WeatherDataImportRow> rows;
        try (final InputStream is = fileOpener.open(filePath)) {
            rows = importer.importFrom(is);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to read file: " + e.getMessage(), e);
        }

        return service.processRows(rows);
    }
}