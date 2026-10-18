package aiSafe.app.backoffice.console.presentation.simulationreport;

import aiSafe.Application;
import aiSafe.simulationreport.application.GenerateSimulationReportController;
import aiSafe.simulationreport.domain.SimulationReport;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

public class GenerateSimulationReportUI extends AbstractUI {

    private static final String DEFAULT_OUTPUT_DIR = "reports";
    private static final String OUTPUT_DIR_PROPERTY = "simulation.report.outputDir";

    private final GenerateSimulationReportController controller = new GenerateSimulationReportController();

    @Override
    protected boolean doShow() {
        try {
            System.out.println("=== Generate Simulation Report (US111) ===");

            final String configuredOutputDir = Application.settings().getProperty(OUTPUT_DIR_PROPERTY);
            final String defaultOutputDir = configuredOutputDir == null || configuredOutputDir.isBlank()
                    ? DEFAULT_OUTPUT_DIR
                    : configuredOutputDir.trim();

            System.out.println("(1) Where to write the NEW report file.");
            final String outputDirectory = Console.readLine(
                    "Output directory [" + defaultOutputDir + "] (folder only, press Enter for default): "
            );
            final String resolvedOutputDir = outputDirectory == null || outputDirectory.isBlank()
                    ? defaultOutputDir
                    : outputDirectory.trim();
            if (resolvedOutputDir.toLowerCase().endsWith(".txt")) {
                throw new IllegalArgumentException(
                        "Output directory must be a folder (e.g. reports), not a .txt file path."
                );
            }

            System.out.println("(2) Existing SCOMP report used as simulation input.");
            final String simulationLogPath = Console.readNonEmptyLine(
                    "Path to SCOMP report file (e.g. SCOMP/reports/report_2026-05-16_11-22-07.txt): ",
                    "Simulation log path is mandatory."
            );

            final SimulationReport report = controller.generateReport(resolvedOutputDir, simulationLogPath);

            System.out.println("Report generated successfully.");
            if (report.filePath() != null) {
                System.out.println("File: " + report.filePath().value());
            }
            System.out.println("Validation status: " + report.validationStatus());
            System.out.println("Total flights: " + report.totalFlights().value());
            System.out.println("Total violations: " + report.totalViolations().value());

            return true;
        } catch (final RuntimeException ex) {
            System.err.println("Error: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Generate Simulation Report";
    }
}
