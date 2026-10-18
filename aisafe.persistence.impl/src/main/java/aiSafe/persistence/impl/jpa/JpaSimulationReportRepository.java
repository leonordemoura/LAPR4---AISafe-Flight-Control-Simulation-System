package aiSafe.persistence.impl.jpa;

import aiSafe.Application;
import aiSafe.simulationreport.domain.SimulationReport;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JpaSimulationReportRepository extends JpaAutoTxRepository<SimulationReport, Long, Long>
        implements SimulationReportRepository {

    public JpaSimulationReportRepository(final TransactionalContext autoTx) {
        super(autoTx, "id");
    }

    public JpaSimulationReportRepository(final String puname) {
        super(puname, Application.settings().getExtendedPersistenceProperties(), "id");
    }

    @Override
    public <S extends SimulationReport> S save(final S report) {
        if (report == null) {
            throw new IllegalArgumentException("Report is required.");
        }
        return super.save(report);
    }

    @Override
    public Optional<SimulationReport> findLatestByFlightDesignator(final String designator) {
        if (designator == null || designator.isBlank()) {
            return Optional.empty();
        }

        SimulationReport latest = null;
        for (final SimulationReport report : findAll()) {
            if (!designator.equals(report.relatedFlightDesignator())) {
                continue;
            }
            if (latest == null
                    || report.reportDate().generatedAt().isAfter(latest.reportDate().generatedAt())) {
                latest = report;
            }
        }
        return Optional.ofNullable(latest);
    }

    @Override
    public List<SimulationReport> findByPeriod(final YearMonth period) {
        if (period == null) throw new IllegalArgumentException("Period cannot be null.");

        final LocalDateTime periodStart = period.atDay(1).atStartOfDay();
        final LocalDateTime periodEnd = period.plusMonths(1).atDay(1).atStartOfDay();

        return entityManager()
                .createQuery(
                        """
                        SELECT sr FROM SimulationReport sr
                        WHERE sr.reportDate.generatedAt >= :periodStart
                          AND sr.reportDate.generatedAt < :periodEnd
                        """,
                        SimulationReport.class)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();
    }

    @Override
    public List<SimulationReport> findByPeriodAndDesignators(final YearMonth period,
                                                             final List<String> designators) {
        if (period == null)      throw new IllegalArgumentException("Period cannot be null.");
        if (designators == null) throw new IllegalArgumentException("Designators cannot be null.");
        if (designators.isEmpty()) return Collections.emptyList();

        final LocalDateTime periodStart = period.atDay(1).atStartOfDay();
        final LocalDateTime periodEnd = period.plusMonths(1).atDay(1).atStartOfDay();

        return entityManager()
                .createQuery(
                        """
                        SELECT sr FROM SimulationReport sr
                        WHERE sr.relatedFlightDesignator IN :designators
                          AND sr.reportDate.generatedAt >= :periodStart
                          AND sr.reportDate.generatedAt < :periodEnd
                        """,
                        SimulationReport.class)
                .setParameter("designators", designators)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();
    }
}
