package aiSafe.simulationreport.application;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SCOMP simulation report files into {@link SimulationRunResultDTO}.
 * Supports Sprint 2 (Time/Position/Altitude) and Sprint 3 (Pos/Alt/Vel + VERDICT) layouts.
 */
public class ScompSimulationResultAdapter {

    private static final int SYNTHETIC_HISTORY_BASE_MINUTES = 540;

    private static final Pattern TOTAL_FLIGHTS = Pattern.compile("^Total Flights:\\s*(\\d+)\\s*$", Pattern.MULTILINE);
    private static final Pattern TOTAL_VIOLATIONS = Pattern.compile("^Total Violations:\\s*(\\d+)\\s*$", Pattern.MULTILINE);
    private static final Pattern VIOLATION_THRESHOLD = Pattern.compile("^Violation Threshold:\\s*(\\d+)\\s*$", Pattern.MULTILINE);

    private static final Pattern FLIGHT_HEADER_SPRINT2 = Pattern.compile("^Flight\\s+(\\d+):\\s*$", Pattern.MULTILINE);
    private static final Pattern HISTORY_LINE_SPRINT2 = Pattern.compile(
            "^\\s*Time:\\s*(\\d{2}:\\d{2})\\s+Position:\\s*\\(([^,]+),\\s*([^)]+)\\),\\s*Altitude:\\s*([\\d.]+)\\s*$",
            Pattern.MULTILINE
    );

    private static final Pattern FLIGHT_HEADER_SPRINT3 = Pattern.compile(
            "^\\s*Flight\\s+(\\d+)\\s*\\(Total Positions:\\s*(\\d+),\\s*Violations:\\s*(\\d+),\\s*Performance Score:\\s*([\\d.]+)%\\):\\s*$",
            Pattern.MULTILINE
    );
    private static final Pattern POS_LINE_SPRINT3 = Pattern.compile(
            "^\\s*Pos:\\s*\\(([^,]+),\\s*([^)]+)\\)\\s+Alt:\\s*([\\d.-]+)\\s+Vel:\\s*\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)\\s*$",
            Pattern.MULTILINE
    );

    private static final Pattern DISTANCES = Pattern.compile(
            "^\\s*Horizontal Distance:\\s*([\\d.]+),\\s*Vertical Distance:\\s*([\\d.]+)\\s*$",
            Pattern.MULTILINE
    );
    private static final Pattern DISTANCES_SPRINT3 = Pattern.compile(
            "^\\s*Distance:\\s*Horizontal=([\\d.]+)\\s+Vertical=([\\d.]+)\\s*$",
            Pattern.MULTILINE
    );
    private static final Pattern FLIGHT_STATE_LINE_SPRINT2 = Pattern.compile(
            "^\\s*Flight\\s+(\\d+):\\s*Position:\\s*\\(([^,]+),\\s*([^)]+)\\),\\s*Altitude:\\s*([\\d.]+),\\s*"
                    + "Velocity Vector\\s*\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)\\s*Time:\\s*(\\d{2}:\\d{2})\\s*$",
            Pattern.MULTILINE
    );
    private static final Pattern SPRINT3_FLIGHT_A = Pattern.compile(
            "Flight A:\\s*(\\d+)\\s*\\R\\s*Position:\\s*\\(([^,]+),\\s*([^)]+)\\)\\s+Altitude:\\s*([\\d.]+)\\s*\\R\\s*Velocity:\\s*\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)",
            Pattern.MULTILINE
    );
    private static final Pattern SPRINT3_FLIGHT_B = Pattern.compile(
            "Flight B:\\s*(\\d+)\\s*\\R\\s*Position:\\s*\\(([^,]+),\\s*([^)]+)\\)\\s+Altitude:\\s*([\\d.]+)\\s*\\R\\s*Velocity:\\s*\\(([^,]+),\\s*([^,]+),\\s*([^)]+)\\)",
            Pattern.MULTILINE
    );
    private static final Pattern SPRINT3_VIOLATION_TIME = Pattern.compile(
            "^\\s*Time:\\s*(\\d{2}:\\d{2})\\s*$",
            Pattern.MULTILINE
    );

    private static final Pattern STATUS_PERFORMANCE = Pattern.compile(
            "^\\s*Performance:\\s*([\\d.]+)\\s*$",
            Pattern.MULTILINE
    );
    private static final Pattern STATUS_TOTAL_VIOLATIONS = Pattern.compile(
            "^\\s*Total Violations:\\s*(\\d+)\\s*$",
            Pattern.MULTILINE
    );

    public SimulationRunResultDTO loadSimulationResult(final String simulationLogPath) {
        if (simulationLogPath == null || simulationLogPath.isBlank()) {
            throw new IllegalArgumentException("Simulation log path is required.");
        }

        final Path path = Path.of(simulationLogPath.trim());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Simulation log file not found: " + path);
        }

        final String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read simulation log: " + path, ex);
        }

        return parseReportContent(content);
    }

    SimulationRunResultDTO parseReportContent(final String content) {
        final boolean validationFailure = content.contains("SIMULATION ABORTED")
                && !TOTAL_FLIGHTS.matcher(content).find();

        if (validationFailure) {
            final String message = extractValidationMessage(content);
            return new SimulationRunResultDTO(
                    0,
                    0,
                    0,
                    true,
                    true,
                    message,
                    null,
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        final int totalFlights = parseIntGroup(TOTAL_FLIGHTS, content, "Total Flights");
        final int totalViolations = parseIntGroup(TOTAL_VIOLATIONS, content, "Total Violations");
        final int violationThreshold = parseIntGroup(VIOLATION_THRESHOLD, content, "Violation Threshold");

        final List<FlightHistorySnapshotDTO> histories = parseFlightHistories(content);
        final List<SafetyViolationEventDTO> violations = parseSafetyViolations(content);
        final List<FlightExecutionStatusDTO> statuses = parseFlightStatuses(content, histories, totalViolations, violationThreshold);

        final boolean failed = content.contains("SIMULATION FAILED");
        final boolean aborted = content.contains("Simulation was aborted")
                || (content.contains("SIMULATION ABORTED") && !content.contains("SIMULATION SUCCEEDED"));
        final boolean earlyStop = failed || aborted;

        final String relatedDesignator = histories.isEmpty()
                ? null
                : String.valueOf(histories.get(0).flightId);

        return new SimulationRunResultDTO(
                totalFlights,
                totalViolations,
                violationThreshold,
                earlyStop,
                false,
                null,
                relatedDesignator,
                histories,
                statuses,
                violations
        );
    }

    private static String extractValidationMessage(final String content) {
        final String[] lines = content.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("========= SIMULATION REPORT =========") && i + 1 < lines.length) {
                final String candidate = lines[i + 1].trim();
                if (!candidate.isEmpty()
                        && !candidate.startsWith("SIMULATION")
                        && !candidate.startsWith("=========")) {
                    return candidate;
                }
            }
        }
        return "Simulation validation failed.";
    }

    private static int parseIntGroup(final Pattern pattern, final String content, final String label) {
        final Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing " + label + " in simulation log.");
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static List<FlightHistorySnapshotDTO> parseFlightHistories(final String content) {
        final int historiesStart = content.indexOf("FLIGHT HISTORIES:");
        if (historiesStart < 0) {
            return List.of();
        }

        final int historiesEnd = indexOfSectionEnd(
                content,
                historiesStart,
                "FLIGHT STATUS:",
                "SAFETY VIOLATIONS:",
                "VERDICT:");
        final String section = content.substring(historiesStart, historiesEnd);

        if (isSprint3HistorySection(section)) {
            return parseFlightHistoriesSprint3(section);
        }
        return parseFlightHistoriesSprint2(section);
    }

    private static boolean isSprint3HistorySection(final String section) {
        return FLIGHT_HEADER_SPRINT3.matcher(section).find() || section.contains("Pos:");
    }

    private static List<FlightHistorySnapshotDTO> parseFlightHistoriesSprint2(final String section) {
        final List<FlightHistorySnapshotDTO> result = new ArrayList<>();
        final Matcher headerMatcher = FLIGHT_HEADER_SPRINT2.matcher(section);
        int lastHeaderEnd = -1;
        int currentFlightId = -1;

        while (headerMatcher.find()) {
            if (currentFlightId > 0 && lastHeaderEnd >= 0) {
                final String block = section.substring(lastHeaderEnd, headerMatcher.start());
                result.add(new FlightHistorySnapshotDTO(currentFlightId, parseHistoryPointsSprint2(block)));
            }
            currentFlightId = Integer.parseInt(headerMatcher.group(1));
            lastHeaderEnd = headerMatcher.end();
        }

        if (currentFlightId > 0 && lastHeaderEnd >= 0) {
            final String block = section.substring(lastHeaderEnd);
            result.add(new FlightHistorySnapshotDTO(currentFlightId, parseHistoryPointsSprint2(block)));
        }

        return result;
    }

    private static List<FlightHistorySnapshotDTO> parseFlightHistoriesSprint3(final String section) {
        final List<FlightHistorySnapshotDTO> result = new ArrayList<>();
        final Matcher headerMatcher = FLIGHT_HEADER_SPRINT3.matcher(section);
        int lastHeaderEnd = -1;
        int currentFlightId = -1;

        while (headerMatcher.find()) {
            if (currentFlightId > 0 && lastHeaderEnd >= 0) {
                final String block = section.substring(lastHeaderEnd, headerMatcher.start());
                result.add(new FlightHistorySnapshotDTO(currentFlightId, parseHistoryPointsSprint3(block)));
            }
            currentFlightId = Integer.parseInt(headerMatcher.group(1));
            lastHeaderEnd = headerMatcher.end();
        }

        if (currentFlightId > 0 && lastHeaderEnd >= 0) {
            final String block = section.substring(lastHeaderEnd);
            result.add(new FlightHistorySnapshotDTO(currentFlightId, parseHistoryPointsSprint3(block)));
        }

        return result;
    }

    private static List<HistoryPointDTO> parseHistoryPointsSprint2(final String block) {
        final List<HistoryPointDTO> points = new ArrayList<>();
        final Matcher lineMatcher = HISTORY_LINE_SPRINT2.matcher(block);
        while (lineMatcher.find()) {
            points.add(new HistoryPointDTO(
                    parseTimeToMinutes(lineMatcher.group(1)),
                    Double.parseDouble(lineMatcher.group(2).trim()),
                    Double.parseDouble(lineMatcher.group(3).trim()),
                    Double.parseDouble(lineMatcher.group(4).trim())
            ));
        }
        return points;
    }

    private static List<HistoryPointDTO> parseHistoryPointsSprint3(final String block) {
        final List<HistoryPointDTO> points = new ArrayList<>();
        final Matcher lineMatcher = POS_LINE_SPRINT3.matcher(block);
        int step = 0;
        while (lineMatcher.find()) {
            points.add(new HistoryPointDTO(
                    SYNTHETIC_HISTORY_BASE_MINUTES + step,
                    Double.parseDouble(lineMatcher.group(1).trim()),
                    Double.parseDouble(lineMatcher.group(2).trim()),
                    Double.parseDouble(lineMatcher.group(3).trim())
            ));
            step++;
        }
        return points;
    }

    private static List<SafetyViolationEventDTO> parseSafetyViolations(final String content) {
        final int violationsStart = content.indexOf("SAFETY VIOLATIONS:");
        if (violationsStart < 0) {
            return List.of();
        }

        final int violationsEnd = indexOfSectionEnd(content, violationsStart, "FLIGHT STATUS:", "VERDICT:");
        final String section = content.substring(violationsStart, violationsEnd);

        if (section.contains("Flight A:") && section.contains("Distance: Horizontal=")) {
            return parseSafetyViolationsSprint3(section);
        }
        return parseSafetyViolationsSprint2(section);
    }

    private static List<SafetyViolationEventDTO> parseSafetyViolationsSprint2(final String section) {
        final String[] blocks = section.split("(?m)^Violation #\\d+:\\s*$");
        final List<SafetyViolationEventDTO> result = new ArrayList<>();

        for (final String block : blocks) {
            if (!block.contains("Horizontal Distance:")) {
                continue;
            }
            final Matcher distanceMatcher = DISTANCES.matcher(block);
            if (!distanceMatcher.find()) {
                continue;
            }
            final double horizontal = Double.parseDouble(distanceMatcher.group(1));
            final double vertical = Double.parseDouble(distanceMatcher.group(2));

            final List<FlightStateParse> states = new ArrayList<>();
            final Matcher stateMatcher = FLIGHT_STATE_LINE_SPRINT2.matcher(block);
            while (stateMatcher.find()) {
                states.add(new FlightStateParse(
                        Integer.parseInt(stateMatcher.group(1)),
                        Double.parseDouble(stateMatcher.group(2).trim()),
                        Double.parseDouble(stateMatcher.group(3).trim()),
                        Double.parseDouble(stateMatcher.group(4).trim()),
                        Double.parseDouble(stateMatcher.group(5).trim()),
                        Double.parseDouble(stateMatcher.group(6).trim()),
                        Double.parseDouble(stateMatcher.group(7).trim()),
                        parseTimeToMinutes(stateMatcher.group(8))
                ));
            }

            if (states.size() >= 2) {
                result.add(toViolationEvent(states.get(0), states.get(1), horizontal, vertical));
            }
        }

        return result;
    }

    private static List<SafetyViolationEventDTO> parseSafetyViolationsSprint3(final String section) {
        final List<SafetyViolationEventDTO> result = new ArrayList<>();
        final String[] blocks = section.split("(?m)^\\s*Violation #\\d+");

        for (final String block : blocks) {
            if (!block.contains("Flight A:") || !block.contains("Flight B:")) {
                continue;
            }
            final Matcher distanceMatcher = DISTANCES_SPRINT3.matcher(block);
            if (!distanceMatcher.find()) {
                continue;
            }
            final double horizontal = Double.parseDouble(distanceMatcher.group(1));
            final double vertical = Double.parseDouble(distanceMatcher.group(2));

            final Matcher flightAMatcher = SPRINT3_FLIGHT_A.matcher(block);
            final Matcher flightBMatcher = SPRINT3_FLIGHT_B.matcher(block);
            if (!flightAMatcher.find() || !flightBMatcher.find()) {
                continue;
            }

            int minutes = 540;
            final Matcher timeMatcher = SPRINT3_VIOLATION_TIME.matcher(block);
            if (timeMatcher.find()) {
                minutes = parseTimeToMinutes(timeMatcher.group(1));
            }

            final FlightStateParse a = new FlightStateParse(
                    Integer.parseInt(flightAMatcher.group(1)),
                    Double.parseDouble(flightAMatcher.group(2).trim()),
                    Double.parseDouble(flightAMatcher.group(3).trim()),
                    Double.parseDouble(flightAMatcher.group(4).trim()),
                    Double.parseDouble(flightAMatcher.group(5).trim()),
                    Double.parseDouble(flightAMatcher.group(6).trim()),
                    Double.parseDouble(flightAMatcher.group(7).trim()),
                    minutes
            );
            final FlightStateParse b = new FlightStateParse(
                    Integer.parseInt(flightBMatcher.group(1)),
                    Double.parseDouble(flightBMatcher.group(2).trim()),
                    Double.parseDouble(flightBMatcher.group(3).trim()),
                    Double.parseDouble(flightBMatcher.group(4).trim()),
                    Double.parseDouble(flightBMatcher.group(5).trim()),
                    Double.parseDouble(flightBMatcher.group(6).trim()),
                    Double.parseDouble(flightBMatcher.group(7).trim()),
                    minutes
            );
            result.add(toViolationEvent(a, b, horizontal, vertical));
        }

        return result;
    }

    private static SafetyViolationEventDTO toViolationEvent(
            final FlightStateParse a,
            final FlightStateParse b,
            final double horizontal,
            final double vertical
    ) {
        return new SafetyViolationEventDTO(
                a.flightId, b.flightId,
                a.minutes, b.minutes,
                a.x, a.y, a.altitude,
                b.x, b.y, b.altitude,
                a.vx, a.vy, a.vz,
                b.vx, b.vy, b.vz,
                horizontal, vertical
        );
    }

    private static List<FlightExecutionStatusDTO> parseFlightStatuses(
            final String content,
            final List<FlightHistorySnapshotDTO> histories,
            final int totalViolations,
            final int violationThreshold
    ) {
        if (content.contains("FLIGHT STATUS:")) {
            return parseFlightStatusesSprint2(content, totalViolations, violationThreshold);
        }
        return parseFlightStatusesFromSprint3(content, histories, totalViolations, violationThreshold);
    }

    private static List<FlightExecutionStatusDTO> parseFlightStatusesSprint2(
            final String content,
            final int totalViolations,
            final int violationThreshold
    ) {
        final int statusStart = content.indexOf("FLIGHT STATUS:");
        if (statusStart < 0) {
            return List.of();
        }

        final int statusEnd = indexOfSectionEnd(content, statusStart, "Simulation ", "VERDICT:");
        final String section = content.substring(statusStart, statusEnd);

        final List<FlightExecutionStatusDTO> result = new ArrayList<>();
        final Matcher headerMatcher = FLIGHT_HEADER_SPRINT2.matcher(section);
        int lastHeaderEnd = -1;
        int currentFlightId = -1;

        while (headerMatcher.find()) {
            if (currentFlightId > 0 && lastHeaderEnd >= 0) {
                result.add(parseFlightStatusBlock(
                        currentFlightId,
                        section.substring(lastHeaderEnd, headerMatcher.start()),
                        totalViolations,
                        violationThreshold));
            }
            currentFlightId = Integer.parseInt(headerMatcher.group(1));
            lastHeaderEnd = headerMatcher.end();
        }

        if (currentFlightId > 0 && lastHeaderEnd >= 0) {
            result.add(parseFlightStatusBlock(
                    currentFlightId,
                    section.substring(lastHeaderEnd),
                    totalViolations,
                    violationThreshold));
        }

        return result;
    }

    private static List<FlightExecutionStatusDTO> parseFlightStatusesFromSprint3(
            final String content,
            final List<FlightHistorySnapshotDTO> histories,
            final int totalViolations,
            final int violationThreshold
    ) {
        final int historiesStart = content.indexOf("FLIGHT HISTORIES:");
        if (historiesStart < 0) {
            return List.of();
        }
        final int historiesEnd = indexOfSectionEnd(
                content,
                historiesStart,
                "FLIGHT STATUS:",
                "SAFETY VIOLATIONS:",
                "VERDICT:");
        final String section = content.substring(historiesStart, historiesEnd);

        final List<FlightExecutionStatusDTO> result = new ArrayList<>();
        final Matcher headerMatcher = FLIGHT_HEADER_SPRINT3.matcher(section);
        while (headerMatcher.find()) {
            final int flightId = Integer.parseInt(headerMatcher.group(1));
            final int perFlightViolations = Integer.parseInt(headerMatcher.group(3));
            final double performance = Double.parseDouble(headerMatcher.group(4));

            final String summary = perFlightViolations > 0
                    ? "Total Violations: " + perFlightViolations
                    : "No violations detected.";

            final Double effectivePerformance;
            if (totalViolations >= violationThreshold && violationThreshold > 0) {
                effectivePerformance = null;
            } else {
                effectivePerformance = performance;
            }

            result.add(new FlightExecutionStatusDTO(
                    flightId,
                    perFlightViolations,
                    effectivePerformance,
                    summary));
        }

        if (!result.isEmpty() || histories.isEmpty()) {
            return result;
        }

        for (final FlightHistorySnapshotDTO history : histories) {
            result.add(new FlightExecutionStatusDTO(
                    history.flightId,
                    0,
                    totalViolations >= violationThreshold && violationThreshold > 0 ? null : 100.0,
                    "No violations detected."));
        }
        return result;
    }

    private static FlightExecutionStatusDTO parseFlightStatusBlock(
            final int flightId,
            final String block,
            final int totalViolations,
            final int violationThreshold
    ) {
        final boolean unablePerformance = block.contains("Unable to calculate performance");
        final Matcher totalViolationsMatcher = STATUS_TOTAL_VIOLATIONS.matcher(block);
        final Matcher performanceMatcher = STATUS_PERFORMANCE.matcher(block);

        int perFlightViolations = 0;
        if (totalViolationsMatcher.find()) {
            perFlightViolations = Integer.parseInt(totalViolationsMatcher.group(1));
        }

        Double performance = null;
        if (!unablePerformance && performanceMatcher.find()) {
            performance = Double.parseDouble(performanceMatcher.group(1));
        }

        final String summary = perFlightViolations > 0
                ? "Total Violations: " + perFlightViolations
                : "No violations detected.";

        if (performance == null && !unablePerformance
                && totalViolations != violationThreshold
                && perFlightViolations == 0) {
            performance = 100.0;
        }

        return new FlightExecutionStatusDTO(flightId, perFlightViolations, performance, summary);
    }

    private static int indexOfSectionEnd(final String content, final int fromIndex, final String... markers) {
        int end = content.length();
        for (final String marker : markers) {
            final int idx = content.indexOf(marker, fromIndex + 1);
            if (idx >= 0 && idx < end) {
                end = idx;
            }
        }
        return end;
    }

    private static int parseTimeToMinutes(final String timeLabel) {
        final String[] parts = timeLabel.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private record FlightStateParse(
            int flightId,
            double x,
            double y,
            double altitude,
            double vx,
            double vy,
            double vz,
            int minutes
    ) {
    }
}
