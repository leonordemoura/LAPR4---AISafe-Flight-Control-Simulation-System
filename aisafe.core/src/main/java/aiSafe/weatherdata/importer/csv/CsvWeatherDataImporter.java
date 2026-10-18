package aiSafe.weatherdata.importer.csv;

import aiSafe.weatherdata.domain.PressureUnit;
import aiSafe.weatherdata.domain.TemperatureUnit;
import aiSafe.weatherdata.domain.WindSpeedUnit;
import aiSafe.weatherdata.importer.WeatherDataImportRow;
import aiSafe.weatherdata.importer.WeatherDataImporter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CsvWeatherDataImporter implements WeatherDataImporter {

    private static final int EXPECTED_COLUMNS = 12;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final double DEFAULT_TEMPERATURE = 15.0;
    private static final TemperatureUnit DEFAULT_TEMP_UNIT = TemperatureUnit.C;
    private static final double DEFAULT_PRESSURE =  1013.25;
    private static final PressureUnit DEFAULT_PRES_UNIT = PressureUnit.HPA;
    private static final String DEFAULT_WEATHER_SOURCE = "UNKNOWN";

    @Override
    public String formatName() {
        return "CSV";
    }

    private double parseDouble(final String value) {
        return Double.parseDouble(value.trim().replace(",", "."));
    }

    @Override
    public List<WeatherDataImportRow> importFrom(final InputStream source) {
        final List<WeatherDataImportRow> rows = new ArrayList<>();

        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(source, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1 || line.isBlank()) continue;

                final String[] cols = line.split(";", -1);
                if (cols.length != EXPECTED_COLUMNS) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + ": expected " + EXPECTED_COLUMNS
                                    + " columns, got " + cols.length);
                }

                try {
                    final String areaCode = cols[0].trim();

                    final double latA   = parseDouble(cols[1]);
                    final double latB   = parseDouble(cols[3]);
                    final double lonA   = parseDouble(cols[2]);
                    final double lonB   = parseDouble(cols[4]);

                    final double minLat = Math.min(latA, latB);
                    final double maxLat = Math.max(latA, latB);
                    final double minLon = Math.min(lonA, lonB);
                    final double maxLon = Math.max(lonA, lonB);

                    final double altInf    = parseDouble(cols[5]);
                    final double altSup    = parseDouble(cols[6]);
                    final double direction = parseDouble(cols[7]);
                    final double speed     = parseDouble(cols[8]);
                    final LocalDate day    = LocalDate.parse(cols[9].trim(), DATE_FMT);
                    final LocalTime start  = LocalTime.parse(cols[10].trim(), TIME_FMT);
                    final LocalTime end    = LocalTime.parse(cols[11].trim(), TIME_FMT);

                    final LocalDateTime obsStart = LocalDateTime.of(day, start);
                    final LocalDateTime obsEnd   = LocalDateTime.of(day, end);

                    rows.add(new WeatherDataImportRow(
                            areaCode,
                            obsStart,
                            obsEnd,
                            minLat, maxLat,
                            minLon, maxLon,
                            altInf, altSup,
                            DEFAULT_WEATHER_SOURCE,
                            direction,
                            speed, WindSpeedUnit.KT,
                            DEFAULT_TEMPERATURE, DEFAULT_TEMP_UNIT,
                            DEFAULT_PRESSURE, DEFAULT_PRES_UNIT
                    ));
                } catch (final Exception e) {
                    throw new IllegalArgumentException(
                            "Line " + lineNumber + ": " + e.getMessage(), e);
                }
            }
        } catch (final IllegalArgumentException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Failed to parse CSV: " + e.getMessage(), e);
        }

        return rows;
    }
}