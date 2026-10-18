package aiSafe.flight.application.importplan.scomp;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class ExportFlightPlansToScompResult {

    private final boolean success;
    private final Path outputPath;
    private final List<String> exportedFlightDesignators;
    private final List<String> errors;

    private ExportFlightPlansToScompResult(
            final boolean success,
            final Path outputPath,
            final List<String> exportedFlightDesignators,
            final List<String> errors
    ) {
        this.success = success;
        this.outputPath = outputPath;
        this.exportedFlightDesignators = exportedFlightDesignators;
        this.errors = errors;
    }

    public static ExportFlightPlansToScompResult success(
            final Path outputPath,
            final List<String> exportedFlightDesignators
    ) {
        return new ExportFlightPlansToScompResult(
                true,
                Objects.requireNonNull(outputPath),
                List.copyOf(exportedFlightDesignators),
                List.of());
    }

    public static ExportFlightPlansToScompResult failure(final List<String> errors) {
        return new ExportFlightPlansToScompResult(false, null, List.of(), List.copyOf(errors));
    }

    public boolean isSuccess() {
        return success;
    }

    public Path outputPath() {
        return outputPath;
    }

    public List<String> exportedFlightDesignators() {
        return exportedFlightDesignators;
    }

    public List<String> errors() {
        return errors;
    }
}
