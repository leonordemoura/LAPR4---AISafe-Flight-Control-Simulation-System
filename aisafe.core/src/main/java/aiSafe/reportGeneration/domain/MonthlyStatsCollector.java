package aiSafe.reportGeneration.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightPlan;
import aiSafe.flight.domain.FlightPlanStatus;
import aiSafe.flight.domain.FlightType;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.simulationreport.domain.SimulationReport;
import aiSafe.simulationreport.domain.ValidationStatus;
import aiSafe.simulationreport.repositories.SimulationReportRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ReportDataCollector for the "Monthly Statistics" report type. This class is responsible for querying the necessary repositories to collect data about flights, simulations, and safety violations for a given month and air-control area, and transforming that data into a structured format suitable for generating a comprehensive report.
 * The collected data includes total flight counts, simulation outcomes, safety violation summaries, validation rates, and statistical charts. Each section of the report is built using helper methods that format the data into readable text and simple ASCII-based charts for visualization.
 * @author 2DE - Group 3
 * @version 2.0
 */
public class MonthlyStatsCollector implements ReportDataCollector {

    /** Fixed width for rendering ASCII bar charts in the report sections. */
    private static final int CHART_BAR_WIDTH = 40;

    /** Repository for accessing simulation reports, used to gather data on safety violations and validation outcomes. */
    private final SimulationReportRepository simulationReportRepository;
    /** Repository for accessing flight data, used to gather information on flight operations and plans. */
    private final FlightRepository flightRepository;

    /**
     * Constructs the MonthlyStatsCollector with the necessary repositories. Validates that neither repository is null to ensure that the collector can function properly.
     * @param simulationReportRepository the repository for accessing simulation reports (never {@code null})
     * @param flightRepository the repository for accessing flight data (never {@code null})
     */
    public MonthlyStatsCollector(final SimulationReportRepository simulationReportRepository, final FlightRepository flightRepository) {
        if (simulationReportRepository == null) {
            throw new IllegalArgumentException("SimulationReportRepository cannot be null.");
        }
        if (flightRepository == null) {
            throw new IllegalArgumentException("FlightRepository cannot be null.");
        }

        this.simulationReportRepository = simulationReportRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Collects data for the monthly statistics report by querying the flight and simulation report repositories for the specified period and air-control area. It processes the collected data to compute various metrics such as total flights, simulation outcomes, safety violations, validation rates, and prepares structured content for each section of the report. The method returns a list of ReportSection objects, each containing a title and formatted content ready to be included in the final report.
     * @param period   the month/year the report covers (never {@code null})
     * @param areaCode the air-control area code (never {@code null} or blank)
     * @return a non-empty, ordered list of ReportSection objects containing the collected data and analysis for the monthly statistics report
     */
    @Override
    public List<ReportSection> collect(final YearMonth period, final AreaCode areaCode) {

        final List<Flight> flights = flightRepository.findByPeriodAndArea(period, areaCode);

        final List<String> flightNumbers = flights.stream()
                .map(f -> String.valueOf(f.designator().flightNumber()))
                .toList();

        final List<SimulationReport> simReports = flightNumbers.isEmpty() ? List.of() : simulationReportRepository.findByPeriodAndDesignators(period, flightNumbers);

        final int totalFlights = flights.size();
        final int totalSimulations = simReports.size();

        final long totalViolations = simReports.stream().mapToLong(sr -> sr.totalViolations().value()).sum();

        final List<FlightPlan> plansInPeriod = flights.stream()
                .flatMap(f -> f.flightPlans().stream())
                .filter(fp -> fp.departureDateTime() != null && YearMonth.from(fp.departureDateTime().value()).equals(period))
                .toList();

        final long totalPlans = plansInPeriod.size();
        final long validatedPlans = plansInPeriod.stream().filter(fp -> fp.status() == FlightPlanStatus.VALIDATED).count();

        final double validationRate = totalPlans == 0 ? 0.0 : (validatedPlans * 100.0) / totalPlans;

        final long regularFlights = flights.stream().filter(f -> f.type() == FlightType.REGULAR).count();
        final long charterFlights = flights.size() - regularFlights;

        final List<ReportSection> sections = new ArrayList<>();
        sections.add(buildExecutiveSummary(period, areaCode, totalFlights, totalSimulations, totalViolations, validationRate));
        sections.add(buildFlightOperationsSection(totalFlights, regularFlights, charterFlights, flights));
        sections.add(buildSafetyViolationsSection(totalViolations, simReports));
        sections.add(buildValidationRateSection(validationRate, validatedPlans, totalPlans));
        sections.add(buildStatisticalChartsSection(regularFlights, charterFlights, totalViolations, simReports, validationRate));
        return sections;
    }

    private ReportSection buildExecutiveSummary(final YearMonth period, final AreaCode areaCode, final int totalFlights, final int totalSimulations, final long totalViolations, final double validationRate) {
        final String content = String.format(Locale.US,
                "Reporting period : %s%n"
                        + "Air Control Area : %s%n"
                        + "Total flights    : %d%n"
                        + "Total simulations: %d%n"
                        + "Safety violations: %d%n"
                        + "Validation rate  : %.1f%%%n",
                period, areaCode.value(), totalFlights, totalSimulations, totalViolations, validationRate);

        return new ReportSection("Executive Summary", content);
    }

    private ReportSection buildFlightOperationsSection(final int totalFlights, final long regularFlights, final long charterFlights, final List<Flight> flights) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US,
                "Total flights  : %d%n"
                        + "Regular flights: %d%n"
                        + "Charter flights: %d%n%n",
                totalFlights, regularFlights, charterFlights));

        if (flights.isEmpty()) {
            sb.append("No flight operations were recorded in this period.");
        } else {
            sb.append("Top Routes (by volume):").append(System.lineSeparator());
            final Map<String, Long> byRoute = flights.stream()
                    .map(f -> f.route().routeDesignator().toString())
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            byRoute.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> sb.append(String.format(Locale.US, "  - %s : %d flights%n",
                            entry.getKey(), entry.getValue())));

            sb.append(System.lineSeparator());
            sb.append(String.format(Locale.US, "%-30s  %12s  %12s%n", "Flight", "Type", "Latest plan"));
            sb.append("-".repeat(60)).append(System.lineSeparator());
            for (final Flight flight : flights) {
                final FlightPlan latest = flight.flightPlan();
                final String planStatus = latest == null ? "no plan" : latest.status().toString();
                sb.append(String.format(Locale.US, "%-30s  %12s  %12s%n",
                        flight.designator(), flight.type(), planStatus));
            }
        }

        return new ReportSection("Flight Operations Statistics", sb.toString());
    }

    private ReportSection buildSafetyViolationsSection(final long totalViolations,
                                                       final List<SimulationReport> simReports) {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "Total safety violations: %d%n%n", totalViolations));

        if (totalViolations == 0) {
            sb.append("No safety violations were recorded in this period.");
        } else {
            sb.append("Most frequent violation outcomes:%n");
            final Map<ValidationStatus, Long> byStatus = simReports.stream()
                    .filter(sr -> sr.totalViolations().value() > 0)
                    .collect(Collectors.groupingBy(SimulationReport::validationStatus, Collectors.counting()));

            byStatus.entrySet().stream()
                    .sorted(Map.Entry.<ValidationStatus, Long>comparingByValue().reversed())
                    .forEach(entry -> sb.append(String.format(Locale.US, "  - %s : %d simulation(s)%n",
                            entry.getKey(), entry.getValue())));

            sb.append(System.lineSeparator());
            sb.append(String.format(Locale.US, "%-30s  %10s  %s%n", "Flight", "Violations", "Status"));
            sb.append("-".repeat(60)).append(System.lineSeparator());
            for (final SimulationReport sr : simReports) {
                if (sr.totalViolations().value() > 0) {
                    sb.append(String.format(Locale.US, "%-30s  %10d  %s%n",
                            sr.relatedFlightDesignator(),
                            sr.totalViolations().value(),
                            sr.validationStatus()));
                }
            }
        }
        return new ReportSection("Safety Violations Summary", sb.toString());
    }

    private ReportSection buildValidationRateSection(final double validationRate,
                                                     final long validatedPlans,
                                                     final long totalPlans) {
        final String content = String.format(Locale.US,
                "Validated flight plans : %d / %d%n"
                        + "Validation rate        : %.1f%%%n"
                        + "Assessment             : %s%n",
                validatedPlans, totalPlans,
                validationRate,
                validationRate >= 95.0 ? "SATISFACTORY" : "REQUIRES ATTENTION");

        return new ReportSection("Validation Rate", content);
    }

    private ReportSection buildStatisticalChartsSection(final long regularFlights,
                                                        final long charterFlights,
                                                        final long totalViolations,
                                                        final List<SimulationReport> simReports,
                                                        final double validationRate) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Flight type distribution").append(System.lineSeparator());
        sb.append(renderBar("Regular", regularFlights, regularFlights + charterFlights));
        sb.append(renderBar("Charter", charterFlights, regularFlights + charterFlights));
        sb.append(System.lineSeparator());

        sb.append("Simulation validation outcomes").append(System.lineSeparator());
        final Map<ValidationStatus, Long> outcomes = new LinkedHashMap<>();
        for (final ValidationStatus status : ValidationStatus.values()) {
            outcomes.put(status, 0L);
        }
        simReports.forEach(sr -> outcomes.merge(sr.validationStatus(), 1L, Long::sum));

        final long totalSimulations = simReports.size();
        outcomes.forEach((status, count) ->
                sb.append(renderBar(status.toString(), count, totalSimulations)));

        sb.append(System.lineSeparator());
        sb.append(String.format(Locale.US, "Validation rate overview: %.1f%%%n", validationRate));
        sb.append(renderBar("Validated", Math.round(validationRate), 100));
        sb.append(renderBar("Remaining", Math.max(0, 100 - Math.round(validationRate)), 100));

        sb.append(System.lineSeparator());
        sb.append("Safety violations intensity (affected simulations)").append(System.lineSeparator());
        final long affected = simReports.stream()
                .filter(sr -> sr.totalViolations().value() > 0)
                .count();
        final long clean = totalSimulations - affected;
        sb.append(renderBar("With violations", affected, totalSimulations));
        sb.append(renderBar("Without violations", clean, totalSimulations));
        sb.append(String.format(Locale.US, "%nTotal violation events: %d%n", totalViolations));

        return new ReportSection("Statistical Charts", sb.toString());
    }

    private String renderBar(final String label, final long value, final long max) {
        if (max <= 0) {
            return String.format(Locale.US, "  %-22s [%-40s] %4d%n", label, "", value);
        }
        final int filled = (int) Math.round((value * (double) CHART_BAR_WIDTH) / max);
        final String bar = "#".repeat(Math.max(0, filled))
                + "-".repeat(Math.max(0, CHART_BAR_WIDTH - filled));
        return String.format(Locale.US, "  %-22s [%s] %4d%n", label, bar, value);
    }
}
