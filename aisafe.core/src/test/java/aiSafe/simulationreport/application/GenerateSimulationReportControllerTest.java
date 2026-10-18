package aiSafe.simulationreport.application;

import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.simulationreport.domain.SimulationReport;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import eapli.framework.domain.repositories.TransactionalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateSimulationReportControllerTest {

    @Mock private AuthorizationGuardService authorizationGuardService;
    @Mock private RepositoryFactory repositoryFactory;
    @Mock private GenerateSimulationReportService generateSimulationReportService;
    @Mock private TransactionalContext tx;
    @Mock private SimulationReport report;

    private GenerateSimulationReportController controller;

    @BeforeEach
    void setUp() {
        // Arrange
        doNothing().when(authorizationGuardService).ensureAuthenticatedFlightControlOperator();
        when(repositoryFactory.newTransactionalContext()).thenReturn(tx);
        doNothing().when(tx).beginTransaction();
        doNothing().when(tx).close();

        controller = new GenerateSimulationReportController(
                authorizationGuardService, repositoryFactory, generateSimulationReportService);
    }

    @Test
    void generateReport_delegatesWithTransaction() {
        // Arrange
        when(generateSimulationReportService.generateReport("reports", "log.txt", tx)).thenReturn(report);
        doNothing().when(tx).commit();

        // Act
        final SimulationReport result = controller.generateReport("reports", "log.txt");

        // Assert
        assertSame(report, result);
        verify(authorizationGuardService).ensureAuthenticatedFlightControlOperator();
        verify(generateSimulationReportService).generateReport("reports", "log.txt", tx);
        verify(tx).commit();
        verify(tx).close();
    }

    @Test
    void generateReport_rollsBackOnFailure() {
        // Arrange
        when(generateSimulationReportService.generateReport("reports", "log.txt", tx))
                .thenThrow(new IllegalArgumentException("bad input"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.generateReport("reports", "log.txt"));

        verify(tx).rollback();
        verify(tx, never()).commit();
        verify(tx).close();
    }
}
