package aiSafe.persistence.impl.inmemory;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.simulationreport.domain.SimulationReport;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.lang.reflect.Field;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class InMemorySimulationReportRepository
        extends InMemoryDomainRepository<SimulationReport, Long>
        implements SimulationReportRepository {

    private static final AtomicLong SEQUENCE = new AtomicLong(1L);

    @Override
    public <S extends SimulationReport> S save(final S report) {
        if (report == null) {
            throw new IllegalArgumentException("Report is required.");
        }
        if (report.identity() == null) {
            assignIdentity(report);
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

        final List<SimulationReport> result = new ArrayList<>();
        for (final SimulationReport report : findAll()) {
            if (period.equals(YearMonth.from(report.reportDate().generatedAt()))) {
                result.add(report);
            }
        }
        return result;
    }

    @Override
    public List<SimulationReport> findByPeriodAndDesignators(final YearMonth period,
                                                             final List<String> designators) {
        if (period == null)      throw new IllegalArgumentException("Period cannot be null.");
        if (designators == null) throw new IllegalArgumentException("Designators cannot be null.");

        final Set<String> designatorSet = Set.copyOf(designators);
        final List<SimulationReport> result = new ArrayList<>();

        for (final SimulationReport report : findAll()) {
            if (period.equals(YearMonth.from(report.reportDate().generatedAt()))
                    && designatorSet.contains(report.relatedFlightDesignator())) {
                result.add(report);
            }
        }
        return result;
    }

    private void assignIdentity(final SimulationReport report) {
        try {
            final Field idField = SimulationReport.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(report, SEQUENCE.getAndIncrement());
        } catch (final NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to assign in-memory identity to SimulationReport", ex);
        }
    }
}
