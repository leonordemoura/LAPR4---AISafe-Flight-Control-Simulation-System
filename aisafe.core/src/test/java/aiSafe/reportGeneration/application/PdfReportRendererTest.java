package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.ReportTestFixtures;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfReportRendererTest {

    private final PdfReportRenderer renderer = new PdfReportRenderer();

    @TempDir
    Path tempDir;

    @Test
    void render_exportsValidPdfWithBrandingAndStatistics() throws IOException {
        final var report = ReportTestFixtures.sampleMonthlyReport();

        final String path = renderer.render(report, tempDir.toString());

        assertTrue(path.endsWith(".pdf"));
        final Path pdfPath = Path.of(path);
        assertTrue(Files.exists(pdfPath));
        assertTrue(Files.size(pdfPath) > 1000);

        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            assertTrue(document.getNumberOfPages() > 0);

            final String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("AISafe"));
            assertTrue(text.contains("Monthly Statistics"));
            assertTrue(text.contains("Executive Summary"));
            assertTrue(text.contains("Statistical Charts"));
            assertTrue(text.contains("Flight type distribution"));
            assertTrue(text.contains("Validation rate"));
            assertTrue(text.contains("LIS"));
        }
    }

    @Test
    void render_handlesMinimalReportWithoutSimulationOutcomes() throws IOException {
        final String path = renderer.render(ReportTestFixtures.minimalMonthlyReport(), tempDir.toString());

        try (PDDocument document = Loader.loadPDF(Path.of(path).toFile())) {
            final String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("No simulations recorded in this period."));
        }
    }

    @Test
    void render_rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> renderer.render(null, tempDir.toString()));
        assertThrows(IllegalArgumentException.class,
                () -> renderer.render(ReportTestFixtures.sampleMonthlyReport(), null));
    }
}
