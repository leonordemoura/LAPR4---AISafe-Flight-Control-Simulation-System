package aiSafe.simulationreport.repositories;

import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.simulationreport.domain.SimulationReport;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface SimulationReportRepository extends DomainRepository<Long, SimulationReport> {

    Optional<SimulationReport> findLatestByFlightDesignator(String designator);

    List<SimulationReport> findByPeriod(YearMonth period);

    List<SimulationReport> findByPeriodAndDesignators(YearMonth period, List<String> designators);
}
