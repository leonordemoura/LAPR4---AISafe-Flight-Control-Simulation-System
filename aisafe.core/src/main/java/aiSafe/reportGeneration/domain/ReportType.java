package aiSafe.reportGeneration.domain;

public enum ReportType {
    MONTHLY_STATISTICS ("Monthly Statistics"),
    INCIDENT_ANALYSIS ("Incident Analysis"),
    COMPLIANCE_REPORT ("Compliance Report");

    /** A human-readable description of the report type. */
    private final String description;

    /** Constructs a ReportType with the specified description. */
    ReportType(String description) {
        this.description = description;
    }

    /** Returns the description of the report type. */
    @Override
    public String toString() {
        return description;
    }
}
