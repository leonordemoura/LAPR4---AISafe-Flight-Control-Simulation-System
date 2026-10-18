package aiSafe.reportGeneration.domain;

import jakarta.persistence.Embeddable;

import java.time.YearMonth;

/**
 * Represents the period of a report, defined by a specific month and year. This class is immutable and serves as a value object within the report generation domain.
 */
@Embeddable
public class ReportPeriod {
    /** The period of the report, represented as a YearMonth (e.g., 2024-06 for June 2024). This field is mandatory and cannot be null. */
    private YearMonth period;

    /** Protected no-argument constructor for ORM frameworks. This constructor should not be used directly. */
    protected ReportPeriod() {
        // for ORM
    }

    /**
     * Creates a new ReportPeriod instance with the specified period. The period must not be null.
     * @param period the period of the report, represented as a YearMonth (e.g., 2024-06 for June 2024)
     * @throws IllegalArgumentException if the period is null
     */
    private ReportPeriod(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("Report period cannot be null.");
        }
        this.period = period;
    }

    /**
     * Creates a new ReportPeriod instance for the specified period.
     * @param period the period of the report, represented as a YearMonth (e.g., 2024-06 for June 2024)
     * @return a new ReportPeriod instance with the specified period
     */
    public static ReportPeriod valueOf(YearMonth period) {
        return new ReportPeriod(period);
    }

    /** Returns the period of the report. */
    public YearMonth period() {
        return period;
    }

    /**
     * Returns a string representation of the report period in the format "YYYY-MM".
     * @return a string representation of the report period
     */
    @Override
    public String toString() {
        return period.toString();
    }

    /**
     * Checks if this ReportPeriod is equal to another object. Two ReportPeriod objects are considered equal if they have the same period.
     * @param o the object to compare with this ReportPeriod
     * @return true if the specified object is equal to this ReportPeriod, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportPeriod that = (ReportPeriod) o;
        return period.equals(that.period);
    }

    /**
     * Returns the hash code of this ReportPeriod, which is based on the period field.
     * @return the hash code of this ReportPeriod
     */
    @Override
    public int hashCode() {
        return period.hashCode();
    }
}
