package aiSafe.simulationreport.application;

import aiSafe.simulationreport.domain.*;

public final class SimulationReportTextFormatter {

    private SimulationReportTextFormatter() {
        // utility
    }

    public static String format(final SimulationReport report) {
        if (report.validationStatus() == ValidationStatus.ABORTED
                && report.totalFlights().value() == 0) {
            return formatValidationFailure(report);
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("========= SIMULATION REPORT =========\n");
        sb.append("Total Flights: ").append(report.totalFlights().value()).append('\n');
        sb.append("Total Violations: ").append(report.totalViolations().value()).append('\n');
        sb.append("Violation Threshold: ").append(report.violationThreshold().value()).append('\n');
        sb.append("\n-----------------------------------\n");

        sb.append("\nFLIGHT HISTORIES:\n");
        for (final FlightHistorySnapshot history : report.flightHistories()) {
            sb.append("\nFlight ").append(history.flightId()).append(":\n");
            for (final HistoryPoint point : history.points()) {
                sb.append(" Time: ")
                        .append(point.timestamp().asTimeLabel())
                        .append(" Position: (")
                        .append(String.format("%.2f", point.position().x()))
                        .append(", ")
                        .append(String.format("%.2f", point.position().y()))
                        .append("), Altitude: ")
                        .append(String.format("%.2f", point.altitude()))
                        .append('\n');
            }
        }

        if (!report.safetyViolations().isEmpty()) {
            sb.append("\n-----------------------------------\n");
            sb.append("\nSAFETY VIOLATIONS:\n");
            int index = 1;
            for (final SafetyViolation violation : report.safetyViolations()) {
                sb.append("Violation #").append(index++).append(":\n");
                sb.append("  Time: ").append(violation.flightA().timestamp().asTimeLabel()).append('\n');
                sb.append("  Horizontal Distance: ")
                        .append(String.format("%.4f", violation.horizontalDistance()))
                        .append(", Vertical Distance: ")
                        .append(String.format("%.4f", violation.verticalDistance()))
                        .append('\n');
                appendFlightViolationState(sb, violation.flightA());
                appendFlightViolationState(sb, violation.flightB());
            }
        }

        sb.append("\n-----------------------------------\n");
        sb.append("\nFLIGHT STATUS:\n");
        final int totalViolations = report.totalViolations().value();
        final int threshold = report.violationThreshold().value();
        final boolean earlyStop = report.validationStatus() != ValidationStatus.SUCCEEDED;

        for (final FlightExecutionStatus status : report.flightExecutionStatuses()) {
            sb.append("\nFlight ").append(status.flightId()).append(":\n");
            if (status.violationCount() > 0) {
                sb.append("  Total Violations: ").append(status.violationCount()).append('\n');
            } else {
                sb.append("  ").append(status.summaryMessage()).append('\n');
            }
            status.performance().ifPresentOrElse(
                    perf -> {
                        if (totalViolations != threshold && !earlyStop) {
                            sb.append("  Performance: ").append(String.format("%.2f", perf)).append("\n\n");
                        } else {
                            sb.append("  Unable to calculate performance.\n\n");
                        }
                    },
                    () -> sb.append("  Unable to calculate performance.\n\n")
            );
        }

        appendVerdictFooter(sb, report.validationStatus());
        sb.append("\n========= END OF REPORT =========\n");
        return sb.toString();
    }

    private static String formatValidationFailure(final SimulationReport report) {
        final StringBuilder sb = new StringBuilder();
        final String message = report.validationFailureMessage() != null
                ? report.validationFailureMessage()
                : "Simulation validation failed.";

        sb.append("========= SIMULATION REPORT =========\n");
        sb.append(message).append("\n\n");
        sb.append("SIMULATION ABORTED :(\n");
        sb.append("\n========= END OF REPORT =========\n");
        return sb.toString();
    }

    private static void appendFlightViolationState(final StringBuilder sb, final FlightViolationState state) {
        sb.append("  Flight ").append(state.flightId())
                .append(": Position: (")
                .append(String.format("%.2f", state.position().x()))
                .append(", ")
                .append(String.format("%.2f", state.position().y()))
                .append("), Altitude: ")
                .append(String.format("%.2f", state.altitude()))
                .append(", Velocity Vector (");
        state.velocity().ifPresentOrElse(
                velocity -> sb.append(String.format("%.2f", velocity.vx()))
                        .append(", ")
                        .append(String.format("%.2f", velocity.vy()))
                        .append(", ")
                        .append(String.format("%.2f", velocity.vz())),
                () -> sb.append("0.00, 0.00, 0.00")
        );
        sb.append(") Time: ").append(state.timestamp().asTimeLabel()).append('\n');
    }

    private static void appendVerdictFooter(final StringBuilder sb, final ValidationStatus status) {
        switch (status) {
            case FAILED -> {
                sb.append("Simulation was aborted due to reaching the violation threshold.\n");
                sb.append("SIMULATION FAILED :(");
            }
            case ABORTED -> {
                sb.append("Simulation was aborted before completion.\n");
                sb.append("SIMULATION ABORTED :(");
            }
            case SUCCEEDED -> {
                sb.append("Simulation completed successfully without reaching the violation threshold.\n");
                sb.append("SIMULATION SUCCEEDED :)\n");
            }
        }
    }
}
