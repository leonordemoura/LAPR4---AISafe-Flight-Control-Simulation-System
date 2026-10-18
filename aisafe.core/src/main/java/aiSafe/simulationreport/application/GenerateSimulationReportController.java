package aiSafe.simulationreport.application;

import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.simulationreport.domain.SimulationReport;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;

import java.util.Objects;

@UseCaseController
public class GenerateSimulationReportController {

    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory repositoryFactory;
    private final GenerateSimulationReportService generateSimulationReportService;

    public GenerateSimulationReportController() {
        this(
                new AuthorizationGuardService(),
                PersistenceContext.repositories(),
                new GenerateSimulationReportService(PersistenceContext.repositories())
        );
    }

    GenerateSimulationReportController(
            final AuthorizationGuardService authorizationGuardService,
            final RepositoryFactory repositoryFactory,
            final GenerateSimulationReportService generateSimulationReportService
    ) {
        this.authorizationGuardService = Objects.requireNonNull(authorizationGuardService);
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
        this.generateSimulationReportService = Objects.requireNonNull(generateSimulationReportService);
    }

    public SimulationReport generateReport(final String outputDirectory, final String simulationLogPath) {
        authorizationGuardService.ensureAuthenticatedFlightControlOperator();

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();
        try {
            final SimulationReport result = generateSimulationReportService.generateReport(
                    outputDirectory, simulationLogPath, tx);
            tx.commit();
            return result;
        } catch (final RuntimeException ex) {
            tx.rollback();
            throw ex;
        } finally {
            tx.close();
        }
    }
}
