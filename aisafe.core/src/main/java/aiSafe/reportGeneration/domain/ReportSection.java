package aiSafe.reportGeneration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Represents a section of a report, containing a title and content.
 */
@Embeddable
public class ReportSection {
    /** The title of the report section. */
    @Column(nullable = false)
    private String title;
    /** The content of the report section. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Protected no-arg constructor for ORM. */
    protected ReportSection() {
        // Required by JPA
    }

    /**
     * Creates a new ReportSection with the specified title and content.
     * @param title The title of the report section.
     * @param content The content of the report section.
     */
    public ReportSection(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * Returns the title of the report section.
     * @return The title of the report section.
     */
    public String title() {
        return title;
    }

    /**
     * Returns the content of the report section.
     * @return The content of the report section.
     */
    public String content() {
        return content;
    }

    /**
     * Returns a string representation of the ReportSection, including its title and content.
     * @return A string representation of the ReportSection.
     */
    @Override
    public String toString() {
        return "ReportSection{" +
                "title=" + title +
                ", content='" + content + '\'' +
                '}';
    }


    /**
     * Checks if this ReportSection is equal to another object. Two ReportSections are considered equal if they have the same title and content.
     * @param o The object to compare with this ReportSection.
     * @return true if the specified object is equal to this ReportSection, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (title == null || content == null) return false;
        ReportSection that = (ReportSection) o;
        return title.equals(that.title) && content.equals(that.content);
    }

    /**
     * Returns the hash code of this ReportSection, which is based on its title and content.
     * @return The hash code of this ReportSection.
     */
    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
