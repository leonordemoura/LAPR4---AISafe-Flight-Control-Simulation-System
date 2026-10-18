package aiSafe.simulationreport.application;

import aiSafe.simulationreport.domain.ReportFilePath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SimulationReportFileWriterTest {

    private final SimulationReportFileWriter writer = new SimulationReportFileWriter();

    @TempDir
    Path tempDir;

    @Test
    void write_createsTimestampedReportFile() throws Exception {
        // Arrange
        final Path outputDir = tempDir.resolve("out");

        // Act
        final ReportFilePath path = writer.write("sample report", outputDir.toString());

        // Assert
        assertTrue(Files.exists(Path.of(path.value())));
        assertTrue(path.value().contains("report_"));
        assertEquals("sample report", Files.readString(Path.of(path.value())));
    }

    @Test
    void write_defaultsToReportsSubdirectory() throws Exception {
        // Arrange
        final Path base = tempDir.resolve("nested");

        // Act
        final ReportFilePath path = writer.write("content", base.toString());

        // Assert
        assertTrue(path.value().contains("report_"));
        assertTrue(Files.exists(Path.of(path.value())));
    }

    @Test
    void write_rejectsBlankContent() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> writer.write("  ", tempDir.toString()));
    }

    @Test
    void write_rejectsNullContent() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> writer.write(null, tempDir.toString()));
    }

    @Test
    void write_acceptsBlankOutputDirectoryAsDefault() throws Exception {
        // Arrange — default folder is ${user.dir}/reports; isolate with user.dir so Maven cwd is not polluted
        final Path isolatedCwd = tempDir.resolve("cwd");
        Files.createDirectories(isolatedCwd);
        final String previous = System.getProperty("user.dir");
        System.setProperty("user.dir", isolatedCwd.toString());

        try {
            // Act
            final ReportFilePath path = writer.write("sample report", "   ");

            // Assert
            final Path written = isolatedCwd.resolve("reports").resolve(
                    Path.of(path.value()).getFileName());
            assertTrue(Files.exists(written));
            assertEquals("sample report", Files.readString(written));
        } finally {
            System.setProperty("user.dir", previous);
        }
    }

    @Test
    void write_throwsWhenOutputPathIsExistingFile(@TempDir Path tempDir) throws Exception {
        // Arrange
        final Path existingFile = tempDir.resolve("not-a-directory.txt");
        Files.writeString(existingFile, "occupied");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> writer.write("content", existingFile.toString()));
    }

    @Test
    void write_usesDefaultReportsDirectoryWhenOutputIsNull() throws Exception {
        // Arrange
        final Path workingDir = tempDir.resolve("cwd");
        Files.createDirectories(workingDir);
        final String previous = System.getProperty("user.dir");
        System.setProperty("user.dir", workingDir.toString());

        try {
            // Act
            final ReportFilePath path = writer.write("simulation report body", null);

            // Assert
            final Path written = workingDir.resolve("reports").resolve(
                    Path.of(path.value()).getFileName());
            assertTrue(Files.exists(written));
            assertEquals("simulation report body", Files.readString(written));
        } finally {
            System.setProperty("user.dir", previous);
        }
    }
}
