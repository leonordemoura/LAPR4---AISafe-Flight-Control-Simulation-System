package aiSafe.flight.application.importplan;

import aiSafe.flight.dsl.FlightDslValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * US121 – validates sample DSL files shipped in {@code FlightPlans/}.
 */
class ImportFlightPlanSampleFilesTest {

    private static final Path FLIGHT_PLANS_DIR = resolveFlightPlansDir();

    private static Path resolveFlightPlansDir() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int depth = 0; depth < 4; depth++) {
            final Path candidate = current.resolve("FlightPlans");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            final Path parent = current.getParent();
            if (parent == null) {
                break;
            }
            current = parent;
        }
        return Paths.get(System.getProperty("user.dir")).resolve("FlightPlans");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "valid-single-leg.flight",
            "valid-two-legs.flight",
            "valid-three-legs.flight",
            "multiple-segments.flight"
    })
    void ensureValidSampleFilesPassDslValidation(final String fileName) throws IOException {
        assumeFlightPlansDirExists();
        final Path file = FLIGHT_PLANS_DIR.resolve(fileName);
        assertTrue(Files.isRegularFile(file), () -> "Missing sample file: " + file);

        final var result = FlightDslValidator.validate(Files.readString(file));
        assertTrue(result.isValid(), () -> fileName + " should be valid: " + result.formattedErrors());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "test-width-wind-invalid.flight",
            "error-invalid-flightDesignator.flight",
            "error-invalid-segment.flight",
            "error-mismatch-airport.flight",
            "different-fuel-units.flight"
    })
    void ensureInvalidSampleFilesAreRejected(final String fileName) throws IOException {
        assumeFlightPlansDirExists();
        final Path file = FLIGHT_PLANS_DIR.resolve(fileName);
        assertTrue(Files.isRegularFile(file), () -> "Missing sample file: " + file);

        final var result = FlightDslValidator.validate(Files.readString(file));
        assertFalse(result.isValid(), () -> fileName + " should be rejected");
        assertFalse(result.errors().isEmpty());
        assertTrue(result.formattedErrors().stream().anyMatch(line -> !line.isBlank()));
    }

    @Test
    void ensureWidthWindInvalidFileReportsSemanticErrors() throws IOException {
        assumeFlightPlansDirExists();
        final Path file = FLIGHT_PLANS_DIR.resolve("test-width-wind-invalid.flight");
        final var result = FlightDslValidator.validate(Files.readString(file));

        assertFalse(result.isValid());
        final String joined = String.join(" ", result.formattedErrors()).toLowerCase();
        assertTrue(joined.contains("width") || joined.contains("wind"),
                () -> "Expected width/wind semantic errors: " + joined);
    }

    @Test
    void dslImporter_mapsUs120ValidationErrorsWithLocation() {
        final var result = new DslFlightPlanImporter().importFromContent(
                """
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
                            width 0 km
                            wind 0 deg 0 kt
                        }
                        fuel 10 kg
                    }
                }
                """);

        assertTrue(result.isPresent());
        assertFalse(result.get().success());
        final ImportFlightPlanError error = result.get().errors().getFirst();
        assertEquals(ImportFlightPlanErrorCode.DSL_VALIDATION, error.code());
        assertNotNull(error.line());
        assertNotNull(error.column());
        assertFalse(error.format().isBlank());
    }

    private static void assumeFlightPlansDirExists() {
        assumeTrue(Files.isDirectory(FLIGHT_PLANS_DIR),
                () -> "FlightPlans directory not found at " + FLIGHT_PLANS_DIR);
    }
}
