package aiSafe.persistence.impl.inmemory;

import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.reportGeneration.repositories.ReportRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryReportRepository
        extends InMemoryDomainRepository<Report, Long>
        implements ReportRepository {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public Iterable<Report> findByPeriodAndArea(final YearMonth period, final String areaCode) {
        if (period == null) throw new IllegalArgumentException("Period cannot be null.");
        if (areaCode == null || areaCode.isBlank()) {
            throw new IllegalArgumentException("Area code cannot be null or blank.");
        }

        final List<Report> result = new ArrayList<>();
        for (final Report report : findAll()) {
            if (report.period().period().equals(period)
                    && areaCode.equals(report.areaCode().value())) {
                result.add(report);
            }
        }
        return result;
    }

    @Override
    public Iterable<Report> findByType(final ReportType type) {
        if (type == null) throw new IllegalArgumentException("Report type cannot be null.");

        final List<Report> result = new ArrayList<>();
        for (final Report report : findAll()) {
            if (report.type() == type) {
                result.add(report);
            }
        }
        return result;
    }

    private void assignIdentity(final Report entity) {
        try {
            final var field = Report.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, SEQUENCE.getAndIncrement());
        } catch (final Exception ex) {
            throw new IllegalStateException(
                    "Unable to assign in-memory identity to Flight", ex);
        }
    }
}
