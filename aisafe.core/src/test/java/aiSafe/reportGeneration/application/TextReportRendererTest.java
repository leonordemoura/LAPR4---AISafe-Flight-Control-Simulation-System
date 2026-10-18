package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.ReportTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TextReportRendererTest {

    private final TextReportRenderer renderer = new TextReportRenderer();

    @TempDir
    Path tempDir;

    @Test
    void render_exportsStructuredTextFile() throws IOException {
        final var report = ReportTestFixtures.sampleMonthlyReport();

        final String path = renderer.render(report, tempDir.toString());

        assertTrue(path.endsWith(".txt"));
        assertTrue(Files.exists(Path.of(path)));

        final String content = Files.readString(Path.of(path));
        assertTrue(content.contains("Monthly Statistics"));
        assertTrue(content.contains("Executive Summary"));
        assertTrue(content.contains("Safety Violations Summary"));
        assertTrue(content.contains("Statistical Charts"));
        assertTrue(content.contains("LIS"));
        assertTrue(content.contains("End of report"));
    }

    @Test
    void render_rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class, () -> renderer.render(null, tempDir.toString()));
        assertThrows(IllegalArgumentException.class,
                () -> renderer.render(ReportTestFixtures.sampleMonthlyReport(), null));
    }
}
