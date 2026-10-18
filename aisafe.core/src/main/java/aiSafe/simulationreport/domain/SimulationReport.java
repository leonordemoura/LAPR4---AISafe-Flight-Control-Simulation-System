package aiSafe.simulationreport.domain;

import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "simulation_report")
public class SimulationReport implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private ReportDate reportDate;

    @Embedded
    private TotalFlights totalFlights;

    @Embedded
    private TotalViolations totalViolations;

    @Embedded
    private ViolationThreshold violationThreshold;

    @Embedded
    private ReportFilePath filePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    @Column(name = "related_flight_designator")
    private String relatedFlightDesignator;

    @Transient
    private List<FlightHistorySnapshot> flightHistories = new ArrayList<>();

    @Transient
    private List<FlightExecutionStatus> flightExecutionStatuses = new ArrayList<>();

    @Transient
    private List<SafetyViolation> safetyViolations = new ArrayList<>();

    @Transient
    private String validationFailureMessage;

    protected SimulationReport() {
        // for ORM
    }

    public SimulationReport(
            final ReportDate reportDate,
            final TotalFlights totalFlights,
            final TotalViolations totalViolations,
            final ViolationThreshold violationThreshold,
            final ValidationStatus validationStatus,
            final List<FlightHistorySnapshot> flightHistories,
            final List<FlightExecutionStatus> flightExecutionStatuses,
            final List<SafetyViolation> safetyViolations,
            final String relatedFlightDesignator,
            final String validationFailureMessage
    ) {
        this.reportDate = Objects.requireNonNull(reportDate, "Report date is required.");
        this.totalFlights = Objects.requireNonNull(totalFlights, "Total flights is required.");
        this.totalViolations = Objects.requireNonNull(totalViolations, "Total violations is required.");
        this.violationThreshold = Objects.requireNonNull(violationThreshold, "Violation threshold is required.");
        this.validationStatus = Objects.requireNonNull(validationStatus, "Validation status is required.");
        if (flightHistories != null) {
            this.flightHistories = new ArrayList<>(flightHistories);
        }
        if (flightExecutionStatuses != null) {
            this.flightExecutionStatuses = new ArrayList<>(flightExecutionStatuses);
        }
        if (safetyViolations != null) {
            this.safetyViolations = new ArrayList<>(safetyViolations);
        }
        this.relatedFlightDesignator = relatedFlightDesignator;
        this.validationFailureMessage = validationFailureMessage;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof SimulationReport that)) {
            return false;
        }
        return Objects.equals(this.identity(), that.identity());
    }

    @Override
    public Long identity() {
        return id;
    }

    public void attachFilePath(final ReportFilePath path) {
        this.filePath = Objects.requireNonNull(path, "File path is required.");
    }

    public ReportDate reportDate() {
        return reportDate;
    }

    public TotalFlights totalFlights() {
        return totalFlights;
    }

    public TotalViolations totalViolations() {
        return totalViolations;
    }

    public ViolationThreshold violationThreshold() {
        return violationThreshold;
    }

    public ReportFilePath filePath() {
        return filePath;
    }

    public ValidationStatus validationStatus() {
        return validationStatus;
    }

    public String relatedFlightDesignator() {
        return relatedFlightDesignator;
    }

    public List<FlightHistorySnapshot> flightHistories() {
        return Collections.unmodifiableList(flightHistories);
    }

    public List<FlightExecutionStatus> flightExecutionStatuses() {
        return Collections.unmodifiableList(flightExecutionStatuses);
    }

    public List<SafetyViolation> safetyViolations() {
        return Collections.unmodifiableList(safetyViolations);
    }

    public String validationFailureMessage() {
        return validationFailureMessage;
    }
}
