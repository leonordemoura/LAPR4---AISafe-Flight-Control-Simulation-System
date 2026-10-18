package aiSafe.reportGeneration.domain;

import jakarta.persistence.Embeddable;

/**
 * Represents the title of a report. This class is an embeddable value object that encapsulates the title of a report. It ensures that the title is not null or empty and provides a factory method for creating instances.
 */
@Embeddable
public class ReportTitle {
    /** The title of the report. */
    private String title;

    /** Protected no-arg constructor for ORM. */
    protected ReportTitle() {
        // for ORM
    }

    /**
     * Creates a new ReportTitle instance with the specified title. The title must not be null or empty.
     * @param title the title of the report
     * @throws IllegalArgumentException if the title is null or empty
     */
    private ReportTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Report title cannot be null or empty.");
        }
        this.title = title;
    }

    /**
     * Creates a new ReportTitle instance with the specified title. The title must not be null or empty.
     * @param title the title of the report
     * @return a new ReportTitle instance with the specified title
     */
    public static ReportTitle valueOf(String title) {
        return new ReportTitle(title);
    }

    /** Returns the title of the report. */
    public String title() {
        return title;
    }

    /**
     * Returns a string representation of the report title, which is simply the title itself.
     * @return a string representation of the report title
     */
    @Override
    public String toString() {
        return title;
    }

    /**
     * Checks if this ReportTitle is equal to another object. Two ReportTitle objects are considered equal if they have the same title.
     * @param o the object to compare with this ReportTitle
     * @return true if the specified object is equal to this ReportTitle, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportTitle that = (ReportTitle) o;
        return title.equals(that.title);
    }

    /**
     * Returns a hash code value for the report title, which is based on the title string.
     * @return a hash code value for this ReportTitle
     */
    @Override
    public int hashCode() {
        return title.hashCode();
    }
}
