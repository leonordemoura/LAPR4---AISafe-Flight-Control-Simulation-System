package aiSafe.reportGeneration.domain;

import aiSafe.aircontrolarea.domain.AreaCode;

import java.time.YearMonth;
import java.util.List;

/**
 * Strategy interface for collecting data and building the sections of a report.
 * Each implementation is responsible for a specific {@link ReportType} and knows
 * how to query the necessary repositories and transform raw data into
 * {@link ReportSection} value objects.
 *
 * <p>New report types (e.g. compliance, incident) are supported simply by adding
 * a new implementation — the {@link ReportFactory} registry picks it up without
 * any change to the factory itself (Open/Closed Principle).</p>
 */
public interface ReportDataCollector {

    /**
     * Collects domain data for the given period and air-control area and
     * transforms it into an ordered list of {@link ReportSection}s.
     *
     * @param period   the month/year the report covers (never {@code null})
     * @param areaCode the air-control area code (never {@code null} or blank)
     * @return a non-empty, ordered list of sections ready to be embedded in a {@link Report}
     */
    List<ReportSection> collect(YearMonth period, AreaCode areaCode);
}
