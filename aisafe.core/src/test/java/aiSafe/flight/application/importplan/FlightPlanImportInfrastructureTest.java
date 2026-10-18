package aiSafe.flight.application.importplan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanImportInfrastructureTest {

    @Test
    void formatDetector_recognizes_dsl_json_and_unknown() {
        final FlightPlanFormatDetector detector = new FlightPlanFormatDetector();

        assertEquals(FlightPlanFileFormat.CORE_FLIGHT_DSL,
                detector.detect("flight TP1 { type REGULAR }", "plan.flight"));
        assertEquals(FlightPlanFileFormat.CORE_FLIGHT_DSL,
                detector.detect("  flight TP1 { type REGULAR }", null));
        assertEquals(FlightPlanFileFormat.JSON,
                detector.detect("{ \"Leg\": [] }", "x.json"));
        assertEquals(FlightPlanFileFormat.UNKNOWN,
                detector.detect("random", "notes.txt"));
        assertEquals(FlightPlanFileFormat.UNKNOWN,
                detector.detect("  ", "plan.flight"));
    }

    @Test
    void fileReader_validates_path_size_and_content(@TempDir final Path tempDir) throws Exception {
        final FlightPlanFileReader reader = new FlightPlanFileReader(100);

        assertFalse(reader.read(null).success());
        assertFalse(reader.read(tempDir.resolve("missing.flight")).success());

        final Path empty = tempDir.resolve("empty.flight");
        Files.writeString(empty, "   ", StandardCharsets.UTF_8);
        assertFalse(reader.read(empty).success());

        final Path valid = tempDir.resolve("ok.flight");
        Files.writeString(valid, "flight TP1 { type REGULAR }", StandardCharsets.UTF_8);
        assertTrue(reader.read(valid).success());
        assertEquals("ok.flight", reader.read(valid).fileName());
    }

    @Test
    void fileReader_rejects_oversized_file(@TempDir final Path tempDir) throws Exception {
        final FlightPlanFileReader reader = new FlightPlanFileReader(10);
        final Path big = tempDir.resolve("big.flight");
        Files.writeString(big, "flight TP1234567890", StandardCharsets.UTF_8);
        assertFalse(reader.read(big).success());
    }

    @Test
    void dslImporter_maps_validation_errors() {
        final var result = new DslFlightPlanImporter().importFromContent("not valid dsl {{{");
        assertTrue(result.isPresent());
        assertFalse(result.get().success());
        assertFalse(result.get().errors().isEmpty());
        assertEquals(ImportFlightPlanErrorCode.DSL_VALIDATION, result.get().errors().getFirst().code());
    }

    @Test
    void dslImporter_rejects_semantically_invalid_dsl() {
        final String dsl = """
                flight TP123 {
                    type REGULAR
                    leg {
                        departure OPO 2026-05-11 08:00
                        arrival LIS 2026-05-11 09:00
                        route { origin OPO destination LIS }
                        segment {
                            start (1.0, 2.0)
                            end (1.0, 2.0)
                            altitude 1000 m
                            width 10 km
                            wind 0 deg 0 kt
                        }
                        fuel 10 kg
                    }
                }
                """;
        final var result = new DslFlightPlanImporter().importFromContent(dsl);
        assertTrue(result.isPresent());
        assertFalse(result.get().success());
        assertTrue(result.get().errors().stream().anyMatch(e -> e.message().contains("coordinates")));
    }
}
