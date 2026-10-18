package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.ReportTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompositeReportRendererTest {

    @TempDir
    Path tempDir;

    @Test
    void render_exportsPdfAndTextFiles() throws Exception {
        final CompositeReportRenderer renderer = new CompositeReportRenderer(
                new PdfReportRenderer(),
                new TextReportRenderer());

        final String primaryPath = renderer.render(
                ReportTestFixtures.sampleMonthlyReport(), tempDir.toString());

        assertTrue(primaryPath.endsWith(".pdf"));
        assertTrue(Files.exists(Path.of(primaryPath)));
        assertTrue(Files.exists(tempDir.resolve("pdf")));
        assertTrue(Files.exists(tempDir.resolve("txt")));

        try (var pdfFiles = Files.list(tempDir.resolve("pdf"));
             var txtFiles = Files.list(tempDir.resolve("txt"))) {
            assertEquals(1, pdfFiles.count());
            assertEquals(1, txtFiles.count());
        }
    }

    @Test
    void constructor_rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeReportRenderer());
    }

    @Test
    void render_rejectsNullArguments() {
        final CompositeReportRenderer renderer = new CompositeReportRenderer(
                (report, dir) -> dir + "/x.pdf");

        assertThrows(NullPointerException.class,
                () -> renderer.render(null, tempDir.toString()));
        assertThrows(NullPointerException.class,
                () -> renderer.render(ReportTestFixtures.sampleMonthlyReport(), null));
    }
}
