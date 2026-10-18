package aiSafe.simulationreport.application;

import aiSafe.simulationreport.domain.ReportFilePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimulationReportFileWriter {

    private static final DateTimeFormatter FILE_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public ReportFilePath write(final String reportContent, final String outputDirectory) {
        if (reportContent == null || reportContent.isBlank()) {
            throw new IllegalArgumentException("Report content is required.");
        }

        final Path outputDir = outputDirectory == null || outputDirectory.isBlank()
                ? Path.of(System.getProperty("user.dir")).resolve("reports")
                : Path.of(outputDirectory.trim());
        try {
            Files.createDirectories(outputDir);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to create output directory: " + outputDir, ex);
        }

        final String fileName = "report_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".txt";
        final Path target = outputDir.resolve(fileName);

        try {
            Files.writeString(target, reportContent, StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to write report file: " + target, ex);
        }

        return new ReportFilePath(target.toString());
    }
}
