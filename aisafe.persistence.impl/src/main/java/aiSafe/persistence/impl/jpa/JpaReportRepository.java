package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.reportGeneration.repositories.ReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class JpaReportRepository
        extends JpaAutoTxRepository<Report, Long, Long>
        implements ReportRepository {

    public JpaReportRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaReportRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public Iterable<Report> findByPeriodAndArea(final YearMonth period, final String areaCode) {
        if (period == null) throw new IllegalArgumentException("Period cannot be null.");
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code cannot be null or blank.");
        }

        return entityManager()
                .createQuery(
                        """
                        SELECT r FROM Report r
                        WHERE r.period.period = :period
                          AND r.areaCode.value = :areaCode
                        """,
                        Report.class)
                .setParameter("period", period)
                .setParameter("areaCode", areaCode)
                .getResultList();
    }

    @Override
    public Iterable<Report> findByType(final ReportType type) {
        if (type == null) throw new IllegalArgumentException("Report type cannot be null.");

        final Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        return match("e.type = :type", params);
    }
}
