package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportRenderer;
import aiSafe.reportGeneration.domain.ReportSection;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Adapter (infrastructure) that implements {@link ReportRenderer} by writing
 * the report as a structured plain-text ({@code .txt}) file.
 *
 * <p>The output file is named:
 * <pre>
 *   &lt;outputDirectory&gt;/&lt;reportType&gt;_&lt;period&gt;_&lt;areaCode&gt;.txt
 *   e.g.  reports/MONTHLY_STATISTICS_2026-04_AREA01.txt
 * </pre>
 * </p>
 *
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
public class TextReportRenderer implements ReportRenderer {

    private static final String SEPARATOR = "=".repeat(72);
    private static final String SECTION_SEP = "-".repeat(72);

    /**
     * Renders {@code report} as a human-readable text file inside {@code outputDirectory}.
     *
     * @param report          the fully-assembled report to render (never {@code null})
     * @param outputDirectory path of the directory where the file will be saved
     * @return the absolute path of the created file
     * @throws IllegalArgumentException if the directory cannot be created or the file cannot be written
     */
    @Override
    public String render(final Report report, final String outputDirectory) {
        if (report == null)          throw new IllegalArgumentException("Report cannot be null.");
        if (outputDirectory == null) throw new IllegalArgumentException("Output directory cannot be null.");

        try {
            final Path dir = Paths.get(outputDirectory + "/txt");
            Files.createDirectories(dir);

            final String filename = buildFilename(report);
            final Path   target   = dir.resolve(filename);

            try (BufferedWriter writer = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
                writeHeader(writer, report);
                writeSections(writer, report);
                writeFooter(writer);
            }

            return target.toAbsolutePath().toString();

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to render report to directory: " + outputDirectory, e);
        }
    }

    /** Derives the output filename from the report's metadata. */
    private String buildFilename(final Report report) {
        final String safeArea = report.areaCode().value()
                .replaceAll("[^A-Za-z0-9_\\-]", "_");

        return String.format("monthly_%s_%s.txt",
                report.period().period(),
                safeArea);
    }

    private void writeHeader(final BufferedWriter w, final Report report) throws IOException {
        w.write(SEPARATOR);
        w.newLine();
        w.write("  " + report.title());
        w.newLine();
        w.write(SEPARATOR);
        w.newLine();
        w.write(String.format("  Type      : %s%n", report.type()));
        w.write(String.format("  Period    : %s%n", report.period()));
        w.write(String.format("  Area      : %s%n", report.areaCode()));
        w.write(String.format("  Generated : %s%n", report.header().generatedAt()));
        w.write(String.format("  By        : %s%n", report.header().generatedBy()));
        w.write(SEPARATOR);
        w.newLine();
        w.newLine();
    }

    private void writeSections(BufferedWriter w, Report report) throws IOException {
        int idx = 1;
        for (final ReportSection section : report.sections()) {
            w.write(String.format("%d. %s%n", idx++, section.title()));
            w.write(SECTION_SEP);
            w.newLine();
            w.write(section.content());
            w.newLine();
            w.newLine();
        }
    }

    private void writeFooter(BufferedWriter w) throws IOException {
        w.write(SEPARATOR);
        w.newLine();
        w.write("  End of report - 2DE3");
        w.newLine();
        w.write(SEPARATOR);
        w.newLine();
    }
}