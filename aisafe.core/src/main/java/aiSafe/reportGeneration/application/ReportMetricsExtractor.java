package aiSafe.reportGeneration.application;

import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts structured metrics from a {@link Report} aggregate so that renderers
 * (e.g. {@link PdfReportRenderer}) can draw charts without duplicating domain logic.
 */
final class ReportMetricsExtractor {

    /** Private constructor to prevent instantiation */
    private ReportMetricsExtractor() {
    }

    /**
     * Extracts key metrics from the given report by parsing its sections and content.
     * @param report the report to extract metrics from
     * @return a {@link ReportMetrics} object containing the extracted metrics
     */
    static ReportMetrics extract(final Report report) {
        final ReportMetrics metrics = new ReportMetrics();

        sectionContent(report, "Executive Summary").ifPresent(content -> {
            metrics.totalFlights = firstLong(content, "Total flights");
            metrics.totalSimulations = firstLong(content, "Total simulations");
            metrics.totalViolations = firstLong(content, "Safety violations");
            metrics.validationRate = firstDouble(content, "Validation rate");
        });

        sectionContent(report, "Flight Operations Statistics").ifPresent(content -> {
            metrics.regularFlights = firstLong(content, "Regular flights");
            metrics.charterFlights = firstLong(content, "Charter flights");
        });

        sectionContent(report, "Validation Rate").ifPresent(content -> {
            metrics.validatedPlans = firstLong(content, "Validated flight plans");
            metrics.totalPlans = extractFractionDenominator(content, "Validated flight plans");
            if (metrics.validationRate == 0.0) {
                metrics.validationRate = firstDouble(content, "Validation rate");
            }
        });

        sectionContent(report, "Statistical Charts").ifPresent(content -> {
            metrics.simulationsWithViolations = barValue(content, "With violations");
            metrics.simulationsWithoutViolations = barValue(content, "Without violations");
            metrics.simulationOutcomes.putAll(parseOutcomeBars(content));
        });

        return metrics;
    }

    /**
     * Helper method to find the content of a section by its title.
     * @param report the report to search within
     * @param title the title of the section to find
     * @return an Optional containing the section content if found, or empty if not found
     */
    private static Optional<String> sectionContent(final Report report, final String title) {
        return report.sections().stream()
                .filter(section -> title.equals(section.title()))
                .map(ReportSection::content)
                .findFirst();
    }

    private static long firstLong(final String content, final String label) {
        final Pattern pattern = Pattern.compile(
                Pattern.quote(label) + "\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private static double firstDouble(final String content, final String label) {
        final Pattern pattern = Pattern.compile(
                Pattern.quote(label) + "\\s*:\\s*([\\d.]+)%?", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0.0;
    }

    private static long extractFractionDenominator(final String content, final String label) {
        final Pattern pattern = Pattern.compile(
                Pattern.quote(label) + "\\s*:\\s*(\\d+)\\s*/\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Long.parseLong(matcher.group(2)) : 0L;
    }

    private static long barValue(final String content, final String label) {
        final Pattern pattern = Pattern.compile(
                "\\s{2}" + Pattern.quote(label) + "\\s+\\[[#\\-]+\\]\\s+(\\d+)");
        final Matcher matcher = pattern.matcher(content);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private static Map<String, Long> parseOutcomeBars(final String content) {
        final Map<String, Long> outcomes = new LinkedHashMap<>();
        final int start = content.indexOf("Simulation validation outcomes");
        if (start < 0) {
            return outcomes;
        }

        final int end = content.indexOf("Validation rate overview", start);
        final String block = end > start ? content.substring(start, end) : content.substring(start);

        final Pattern pattern = Pattern.compile("\\s{2}([A-Z_]+)\\s+\\[[#\\-]+\\]\\s+(\\d+)");
        final Matcher matcher = pattern.matcher(block);
        while (matcher.find()) {
            outcomes.put(matcher.group(1), Long.parseLong(matcher.group(2)));
        }
        return outcomes;
    }

    static final class ReportMetrics {
        long totalFlights;
        long totalSimulations;
        long totalViolations;
        long regularFlights;
        long charterFlights;
        long validatedPlans;
        long totalPlans;
        double validationRate;
        long simulationsWithViolations;
        long simulationsWithoutViolations;
        final Map<String, Long> simulationOutcomes = new LinkedHashMap<>();
    }
}
