package aiSafe.flight.application.importplan;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class FlightPlanFileReader {

    static final int DEFAULT_MAX_BYTES = 512_000;

    private final int maxBytes;

    FlightPlanFileReader() {
        this(DEFAULT_MAX_BYTES);
    }

    FlightPlanFileReader(final int maxBytes) {
        this.maxBytes = maxBytes;
    }

    ReadFileResult read(final Path path) {
        if (path == null) {
            return ReadFileResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.FILE_READ_ERROR,
                            "File path is required.")));
        }

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return ReadFileResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.FILE_READ_ERROR,
                            "File not found: " + path)));
        }

        try {
            final long size = Files.size(path);
            if (size > maxBytes) {
                return ReadFileResult.failure(List.of(
                        new ImportFlightPlanError(
                                ImportFlightPlanErrorCode.FILE_TOO_LARGE,
                                "File exceeds maximum size of " + maxBytes + " bytes.")));
            }

            final byte[] bytes = Files.readAllBytes(path);
            if (bytes.length == 0) {
                return ReadFileResult.failure(List.of(
                        new ImportFlightPlanError(
                                ImportFlightPlanErrorCode.EMPTY_FILE,
                                "Flight plan file is empty.")));
            }

            final String content = new String(bytes, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return ReadFileResult.failure(List.of(
                        new ImportFlightPlanError(
                                ImportFlightPlanErrorCode.EMPTY_FILE,
                                "Flight plan file is empty.")));
            }

            return ReadFileResult.success(content, path.getFileName().toString());
        } catch (final IOException ex) {
            return ReadFileResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.FILE_READ_ERROR,
                            "Could not read file: " + ex.getMessage())));
        }
    }

    record ReadFileResult(boolean success, String content, String fileName, List<ImportFlightPlanError> errors) {

        static ReadFileResult success(final String content, final String fileName) {
            return new ReadFileResult(true, content, fileName, List.of());
        }

        static ReadFileResult failure(final List<ImportFlightPlanError> errors) {
            return new ReadFileResult(false, null, null, errors);
        }
    }
}
