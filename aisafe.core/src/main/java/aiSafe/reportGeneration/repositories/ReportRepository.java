package aiSafe.reportGeneration.repositories;

import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportType;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.YearMonth;

/**
 * Repository interface for the Report aggregate root.
 */
public interface ReportRepository extends DomainRepository<Long, Report> {

    /**
     * Finds all reports for a given period and air control area code.
     *
     * @param period   the month/year of the report
     * @param areaCode the air control area code
     * @return all matching reports
     */
    Iterable<Report> findByPeriodAndArea(YearMonth period, String areaCode);

    /**
     * Finds all reports of a given type.
     *
     * @param type the report type
     * @return all reports of the given type
     */
    Iterable<Report> findByType(ReportType type);
}