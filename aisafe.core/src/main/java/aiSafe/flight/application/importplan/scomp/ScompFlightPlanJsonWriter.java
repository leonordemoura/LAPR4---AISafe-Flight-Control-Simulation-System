package aiSafe.flight.application.importplan.scomp;

import aiSafe.flight.domain.FlightDesignator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Writes SCOMP-ready JSON under {@code SCOMP_SPRINT3/json/} (relative to the process working directory).
 */
public final class ScompFlightPlanJsonWriter {

    private static final DateTimeFormatter FILE_STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public Path write(final FlightDesignator designator, final String jsonContent) throws IOException {
        Objects.requireNonNull(designator);
        Objects.requireNonNull(jsonContent);

        final String suffix = designator.airlineCode()
                + designator.flightNumber()
                + (designator.operationalSuffix() == null ? "" : designator.operationalSuffix())
                + "_"
                + FILE_STAMP.format(LocalDateTime.now());
        return write(jsonContent, "Flight_Plan_" + suffix + ".json");
    }

    public Path writeMultiFlightExport(final String jsonContent) throws IOException {
        Objects.requireNonNull(jsonContent);
        return write(jsonContent, "Flight_Plan_" + FILE_STAMP.format(LocalDateTime.now()) + ".json");
    }

    private Path write(final String jsonContent, final String fileName) throws IOException {
        final Path outputDir = resolveOutputDirectory();
        Files.createDirectories(outputDir);

        final Path target = outputDir.resolve(fileName);
        Files.writeString(target, jsonContent, StandardCharsets.UTF_8);
        return target.toAbsolutePath().normalize();
    }

    private static Path resolveOutputDirectory() {
        final String override = System.getProperty("aisafe.scomp.json.dir");
        if (override != null && !override.isBlank()) {
            return Path.of(override.trim());
        }
        return Path.of(System.getProperty("user.dir", "."), "SCOMP_SPRINT3", "json");
    }
}
