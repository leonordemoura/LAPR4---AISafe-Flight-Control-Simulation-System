package aiSafe.reportGeneration.domain;

import aiSafe.aircontrolarea.domain.AreaCode;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "report")
public class Report implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", updatable = false, nullable = false)
    private Long id;

    @Embedded
    private ReportTitle title;

    @Embedded
    private ReportPeriod period;

    @Embedded
    private ReportHeader header;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "area_code", nullable = false))
    private AreaCode areaCode;

    @ElementCollection
    @CollectionTable(
            name = "report_section",
            joinColumns = @JoinColumn(
                    name = "report_id",
                    referencedColumnName = "report_id"
            )
    )
    @OrderColumn(name = "display_order")
    private List<ReportSection> sections = new ArrayList<>();

    protected Report() {
        // for ORM
    }

    /**
     * Creates a new Report with all required fields.
     *
     * @param title    the title of the report
     * @param period   the period the report covers
     * @param header   the header metadata (generated at / by)
     * @param type     the type of report
     * @param sections the ordered list of sections that compose the report
     */
    private Report(ReportTitle title, ReportPeriod period,
                   ReportHeader header, ReportType type, AreaCode areaCode,
                   List<ReportSection> sections) {
        if (title == null) throw new IllegalArgumentException("Report title cannot be null.");
        if (period == null) throw new IllegalArgumentException("Report period cannot be null.");
        if (header == null) throw new IllegalArgumentException("Report header cannot be null.");
        if (type == null) throw new IllegalArgumentException("Report type cannot be null.");
        if (areaCode == null) throw new IllegalArgumentException("Area code cannot be null.");
        if (sections == null || sections.isEmpty()) throw new IllegalArgumentException("Report must have at least one section.");

        this.title = title;
        this.period = period;
        this.header = header;
        this.type = type;
        this.areaCode = areaCode;
        this.sections = new ArrayList<>(sections);
    }

    /**
     * Factory method — creates a fully populated Report instance.
     *
     * @param title    the title of the report
     * @param period   the period the report covers
     * @param header   the header metadata (generated at / by)
     * @param type     the type of report
     * @param sections the ordered list of sections that compose the report
     * @return a new Report instance
     */
    public static Report create(ReportTitle title, ReportPeriod period, ReportHeader header, ReportType type, AreaCode areaCode, List<ReportSection> sections) {
        return new Report(title, period, header, type, areaCode, sections);
    }

    /** Returns the title of the report. */
    public ReportTitle title() {
        return title;
    }

    /** Returns the period this report covers. */
    public ReportPeriod period() {
        return period;
    }

    /** Returns the header metadata of the report. */
    public ReportHeader header() {
        return header;
    }

    /** Returns the type of this report. */
    public ReportType type() {
        return type;
    }

    /** Returns the air-control area this report covers. */
    public AreaCode areaCode() {
        return areaCode;
    }

    /**
     * Returns an unmodifiable view of the report's sections, preserving their display order.
     */
    public List<ReportSection> sections() {
        return Collections.unmodifiableList(sections);
    }

    @Override
    public String toString() {
        return String.format("Report{ id=%s, type=%s, period=%s, title=%s }",
                id, type, period, title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report that = (Report) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Report that = (Report) other;
        return id != null && id.equals(that.id);
    }
}
