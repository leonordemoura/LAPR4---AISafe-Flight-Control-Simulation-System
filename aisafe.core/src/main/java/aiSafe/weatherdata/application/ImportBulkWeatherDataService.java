package aiSafe.weatherdata.application;

import aiSafe.aircontrolarea.domain.*;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.weatherdata.importer.WeatherDataImportResult;
import aiSafe.weatherdata.importer.WeatherDataImportRow;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportBulkWeatherDataService {

    private static final double BOUNDARY_MARGIN = 1.0;
    private static final double ALTITUDE_MARGIN = 1000;
    static final double FEET_TO_METERS = 0.3048;

    private final RegisterWeatherDataController registerController;
    private final RepositoryFactory repositories;

    public ImportBulkWeatherDataService() {
        this.registerController = new RegisterWeatherDataController();
        this.repositories = PersistenceContext.repositories();
    }

    ImportBulkWeatherDataService(final RepositoryFactory repositories,
                                 final RegisterWeatherDataController registerController) {
        this.repositories = repositories;
        this.registerController = registerController;
    }

    public List<WeatherDataImportResult> processRows(final List<WeatherDataImportRow> rows) {
        final Set<String> processedAreas = new HashSet<>();
        final List<WeatherDataImportResult> results = new ArrayList<>();
        int lineNumber = 2;

        for (final WeatherDataImportRow row : rows) {
            try {
                if (!processedAreas.contains(row.areaCode())) {
                    ensureAreaExists(row.areaCode(), rows);
                    processedAreas.add(row.areaCode());
                }
                registerController.registerWeatherData(
                        row.areaCode(),
                        row.observationStart(),
                        row.observationEnd(),
                        row.minLatitude(),  row.maxLatitude(),
                        row.minLongitude(), row.maxLongitude(),
                        row.minAltitude() * FEET_TO_METERS,
                        row.maxAltitude() * FEET_TO_METERS,
                        row.weatherSource(),
                        row.windDirection(),
                        row.windSpeed(),    row.windSpeedUnit(),
                        row.temperature(),  row.temperatureUnit(),
                        row.pressure(),     row.pressureUnit()
                );
                results.add(WeatherDataImportResult.ok(lineNumber));
            } catch (final Exception e) {
                results.add(WeatherDataImportResult.failure(lineNumber, e.getMessage()));
            }
            lineNumber++;
        }
        return results;
    }

    private void ensureAreaExists(final String areaCodeValue,
                                  final List<WeatherDataImportRow> allRows) {
        final TransactionalContext tx = repositories.newTransactionalContext();
        tx.beginTransaction();
        try {
            final AirControlAreaRepository repo = repositories.airControlAreas(tx);
            final AreaCode areaCode = new AreaCode(areaCodeValue);

            if (repo.existsByAreaCode(areaCode)) {
                tx.commit();
                return;
            }

            final List<WeatherDataImportRow> areaRows = allRows.stream()
                    .filter(r -> r.areaCode().equals(areaCodeValue))
                    .toList();

            final double minLat = areaRows.stream().mapToDouble(WeatherDataImportRow::minLatitude).min().orElseThrow()  - BOUNDARY_MARGIN;
            final double maxLat = areaRows.stream().mapToDouble(WeatherDataImportRow::maxLatitude).max().orElseThrow()  + BOUNDARY_MARGIN;
            final double minLon = areaRows.stream().mapToDouble(WeatherDataImportRow::minLongitude).min().orElseThrow() - BOUNDARY_MARGIN;
            final double maxLon = areaRows.stream().mapToDouble(WeatherDataImportRow::maxLongitude).max().orElseThrow() + BOUNDARY_MARGIN;
            final double maxAlt = (areaRows.stream().mapToDouble(WeatherDataImportRow::maxAltitude).max().orElseThrow()
                    * FEET_TO_METERS) + ALTITUDE_MARGIN;

            final List<Coordinate> corners = List.of(
                    new Coordinate(minLat, minLon),
                    new Coordinate(minLat, maxLon),
                    new Coordinate(maxLat, minLon),
                    new Coordinate(maxLat, maxLon)
            );

            final AirControlArea area = new AirControlArea(
                    areaCode,
                    new AreaName(areaCodeValue),
                    new MinFuelAtArrival(1.0),
                    new MaxAltitude(maxAlt),
                    new GeoBoundary(corners)
            );

            repo.save(area);
            tx.commit();
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }
}