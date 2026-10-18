package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportRenderer;
import aiSafe.reportGeneration.domain.ReportSection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter that renders a {@link Report} as a branded PDF with textual sections
 * and vector bar charts for the monthly statistics.
 */
public class PdfReportRenderer implements ReportRenderer {

    private static final float MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);
    private static final float LINE_HEIGHT = 14f;

    private static final Color BRAND_PRIMARY = new Color(0x1A, 0x3C, 0x6E);
    private static final Color BRAND_ACCENT = new Color(0x2E, 0x86, 0xAB);
    private static final Color CHART_COLORS[] = {
            new Color(0x2E, 0x86, 0xAB),
            new Color(0xF1, 0x8F, 0x01),
            new Color(0xC7, 0x3E, 0x1D),
            new Color(0x6A, 0x9B, 0x5B),
            new Color(0x7B, 0x68, 0xEE)
    };

    /**
     * Renders the given report as a PDF file in the specified output directory. The PDF will include a branded header, metadata about the report, sections with their content, and bar charts for the statistical metrics. The method returns the absolute path of the generated PDF file. If any error occurs during rendering (e.g., invalid report data, I/O issues), it throws an IllegalArgumentException with a descriptive message.
     * @param report          the fully-assembled report to render (never {@code null})
     * @param outputDirectory the filesystem path of the target directory (never {@code null})
     * @return the absolute path of the generated PDF file
     */
    @Override
    public String render(final Report report, final String outputDirectory) {
        if (report == null) throw new IllegalArgumentException("Report cannot be null.");
        if (outputDirectory == null) throw new IllegalArgumentException("Output directory cannot be null.");

        try {
            final Path dir = Paths.get(outputDirectory + "/pdf");
            Files.createDirectories(dir);
            final Path target = dir.resolve(buildFilename(report));

            try (PDDocument document = new PDDocument()) {
                writeReport(document, report);
                document.save(target.toFile());
            }

            return target.toAbsolutePath().toString();
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Failed to render PDF report to directory: " + outputDirectory, ex);
        }
    }

    /**
     * Writes the content of the given report into the provided PDF document. This method handles the layout and styling of the PDF, including drawing a branded header, report metadata, section headings, paragraphs, and bar charts for the statistical metrics. It uses a helper class {@link PageWriter} to manage the content stream and pagination. If any I/O error occurs during writing, it throws an IOException.
     * @param document the PDF document to which the report content will be written (never {@code null})
     * @param report  the report whose content will be rendered into the PDF (never {@code null})
     * @throws IOException if an error occurs while writing to the PDF document
     */
    private void writeReport(final PDDocument document, final Report report) throws IOException {
        final PDFont regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        final PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        PageWriter writer = new PageWriter(document, regular, bold);
        writer.drawBrandedHeader(report);
        writer.drawMetadata(report);
        writer.blankLine();

        int sectionIndex = 1;
        for (final ReportSection section : report.sections()) {
            if ("Statistical Charts".equals(section.title())) {
                continue;
            }
            writer.drawSectionHeading(sectionIndex++, section.title());
            writer.drawParagraph(section.content());
            writer.blankLine();
        }

        writer.ensureSpace(220f);
        writer.drawSectionHeading(sectionIndex, "Statistical Charts");
        drawCharts(writer, ReportMetricsExtractor.extract(report));

        writer.drawFooter();
        writer.close();
    }

    /**
     * Draws the statistical charts for the report using the provided PageWriter. It creates bar charts for flight type distribution, simulation validation outcomes, validation rate, and safety violations based on the metrics extracted from the report. If there are no simulations recorded in the period, it displays an appropriate message instead of the validation outcomes chart. The method ensures that there is enough space on the page for each chart and adds new pages as needed. If any I/O error occurs while drawing the charts, it throws an IOException.
     * @param writer the PageWriter used to draw the charts onto the PDF document (never {@code null})
     * @param metrics the ReportMetrics object containing the data to be visualized in the charts (never {@code null})
     * @throws IOException if an error occurs while drawing the charts onto the PDF document
     */
    private void drawCharts(final PageWriter writer,
                            final ReportMetricsExtractor.ReportMetrics metrics) throws IOException {
        writer.drawSubheading("Flight type distribution");
        writer.drawBarChart(
                List.of(
                        new ChartBar("Regular", metrics.regularFlights),
                        new ChartBar("Charter", metrics.charterFlights)
                ),
                Math.max(1, metrics.regularFlights + metrics.charterFlights));

        writer.blankLine();
        writer.drawSubheading("Simulation validation outcomes");
        if (metrics.simulationOutcomes.isEmpty()) {
            writer.drawParagraph("No simulations recorded in this period.");
        } else {
            final long maxOutcome = metrics.simulationOutcomes.values().stream()
                    .mapToLong(Long::longValue).max().orElse(1L);
            writer.drawBarChart(
                    metrics.simulationOutcomes.entrySet().stream()
                            .map(e -> new ChartBar(e.getKey(), e.getValue()))
                            .toList(),
                    Math.max(1, maxOutcome));
        }

        writer.blankLine();
        writer.drawSubheading("Validation rate");
        writer.drawBarChart(
                List.of(
                        new ChartBar("Validated", Math.round(metrics.validationRate)),
                        new ChartBar("Remaining", Math.max(0, 100 - Math.round(metrics.validationRate)))
                ),
                100);

        writer.blankLine();
        writer.drawSubheading("Safety violations (simulations)");
        writer.drawBarChart(
                List.of(
                        new ChartBar("With violations", metrics.simulationsWithViolations),
                        new ChartBar("Without violations", metrics.simulationsWithoutViolations)
                ),
                Math.max(1, metrics.totalSimulations));

        writer.blankLine();
        writer.drawParagraph(String.format(
                "Total violation events: %d | Total flights: %d | Total simulations: %d",
                metrics.totalViolations, metrics.totalFlights, metrics.totalSimulations));
    }

    /**
     * Builds a safe filename for the PDF report based on the report's period and area code. The filename is formatted as "monthly_{period}_{area}.pdf", where {period} is the report's period (e.g., "2024-06") and {area} is a sanitized version of the report's area code (non-alphanumeric characters replaced with underscores). This method ensures that the generated filename is safe for use in filesystems and does not contain any characters that could cause issues. If the report's area code is null or empty, it will be replaced with "unknown" in the filename.
     * @param report the report for which the filename is being generated (never {@code null})
     * @return a safe filename string for the PDF report, based on the report's period and area code
     */
    private String buildFilename(final Report report) {
        final String safeArea = report.areaCode().value()
                .replaceAll("[^A-Za-z0-9_\\-]", "_");
        return String.format("monthly_%s_%s.pdf", report.period().period(), safeArea);
    }

    private record ChartBar(String label, long value) {
    }

    /**
     * Helper class that manages the content stream and pagination for writing the report content into the PDF document. It provides methods for drawing the branded header, report metadata, section headings, paragraphs, and bar charts. The PageWriter ensures that there is enough space on the page for each element and adds new pages as needed. It also handles the styling of the text and charts according to the defined brand colors and fonts. If any I/O error occurs while writing to the PDF document, it throws an IOException.
     */
    private final class PageWriter {
        private final PDDocument document;
        private final PDFont regular;
        private final PDFont bold;
        private PDPage page;
        private PDPageContentStream stream;
        private float y;

        private PageWriter(final PDDocument document, final PDFont regular, final PDFont bold)
                throws IOException {
            this.document = document;
            this.regular = regular;
            this.bold = bold;
            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        private void ensureSpace(final float required) throws IOException {
            if (y - required < MARGIN) {
                newPage();
            }
        }

        private void drawBrandedHeader(final Report report) throws IOException {
            stream.setNonStrokingColor(BRAND_PRIMARY);
            stream.addRect(0, PAGE_HEIGHT - 70, PAGE_WIDTH, 70);
            stream.fill();

            stream.setNonStrokingColor(Color.WHITE);
            writeText(bold, 18, MARGIN, PAGE_HEIGHT - 35, "AISafe — Monthly Statistics Report");
            writeText(regular, 11, MARGIN, PAGE_HEIGHT - 55, report.title().toString());
            y = PAGE_HEIGHT - 90;
        }

        private void drawMetadata(final Report report) throws IOException {
            writeText(regular, 10, MARGIN, y, "Type: " + report.type());
            y -= LINE_HEIGHT;
            writeText(regular, 10, MARGIN, y, "Period: " + report.period());
            y -= LINE_HEIGHT;
            writeText(regular, 10, MARGIN, y, "Area: " + report.areaCode());
            y -= LINE_HEIGHT;
            writeText(regular, 10, MARGIN, y,
                    "Generated: " + report.header().generatedAt() + " by " + report.header().generatedBy());
            y -= LINE_HEIGHT;
        }

        private void drawSectionHeading(final int index, final String title) throws IOException {
            ensureSpace(30f);
            stream.setNonStrokingColor(BRAND_PRIMARY);
            writeText(bold, 12, MARGIN, y, index + ". " + title);
            y -= 6;
            stream.setStrokingColor(BRAND_ACCENT);
            stream.setLineWidth(1.5f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(PAGE_WIDTH - MARGIN, y);
            stream.stroke();
            y -= LINE_HEIGHT;
        }

        private void drawSubheading(final String title) throws IOException {
            ensureSpace(20f);
            stream.setNonStrokingColor(BRAND_ACCENT);
            writeText(bold, 11, MARGIN, y, title);
            y -= LINE_HEIGHT;
        }

        private void drawParagraph(final String text) throws IOException {
            for (final String line : wrap(text, regular, 10, CONTENT_WIDTH)) {
                ensureSpace(LINE_HEIGHT);
                stream.setNonStrokingColor(Color.BLACK);
                writeText(regular, 10, MARGIN, y, line);
                y -= LINE_HEIGHT;
            }
        }

        private void drawBarChart(final List<ChartBar> bars, final long maxValue) throws IOException {
            final float barHeight = 16f;
            final float gap = 8f;
            final float labelWidth = 120f;
            final float chartWidth = CONTENT_WIDTH - labelWidth - 40f;
            int colorIndex = 0;

            for (final ChartBar bar : bars) {
                ensureSpace(barHeight + gap + 4);
                stream.setNonStrokingColor(Color.DARK_GRAY);
                writeText(regular, 9, MARGIN, y - 10, bar.label());

                final float filledWidth = maxValue <= 0
                        ? 0
                        : (float) bar.value / maxValue * chartWidth;

                final float barX = MARGIN + labelWidth;
                final float barY = y - barHeight;

                stream.setNonStrokingColor(new Color(230, 230, 230));
                stream.addRect(barX, barY, chartWidth, barHeight);
                stream.fill();

                if (filledWidth > 0) {
                    stream.setNonStrokingColor(CHART_COLORS[colorIndex % CHART_COLORS.length]);
                    stream.addRect(barX, barY, filledWidth, barHeight);
                    stream.fill();
                }

                stream.setNonStrokingColor(Color.BLACK);
                writeText(regular, 9, barX + chartWidth + 8, y - 10, Long.toString(bar.value()));

                y -= barHeight + gap;
                colorIndex++;
            }
        }

        private void drawFooter() throws IOException {
            ensureSpace(30f);
            stream.setStrokingColor(BRAND_PRIMARY);
            stream.setLineWidth(1f);
            stream.moveTo(MARGIN, y);
            stream.lineTo(PAGE_WIDTH - MARGIN, y);
            stream.stroke();
            y -= LINE_HEIGHT;
            stream.setNonStrokingColor(Color.GRAY);
            writeText(regular, 8, MARGIN, y, "End of report —  2DE3");
        }

        private void blankLine() {
            y -= LINE_HEIGHT;
        }

        private void writeText(final PDFont font, final float size,
                               final float x, final float baselineY, final String text) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(x, baselineY);
            stream.showText(sanitize(text));
            stream.endText();
        }

        private void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private static String sanitize(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace('–', '-')
                .replace('—', '-')
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private static List<String> wrap(final String text, final PDFont font,
                                     final float fontSize, final float maxWidth) throws IOException {
        final List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }

        for (final String paragraph : text.split("\\R")) {
            if (paragraph.isBlank()) {
                lines.add("");
                continue;
            }
            final String[] words = paragraph.trim().split("\\s+");
            StringBuilder current = new StringBuilder();
            for (final String word : words) {
                final String candidate = current.isEmpty() ? word : current + " " + word;
                final float width = font.getStringWidth(sanitize(candidate)) / 1000 * fontSize;
                if (width > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }
        return lines;
    }
}
