package aiSafe.reportGeneration.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.flight.repositories.FlightRepository;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.reportGeneration.domain.MonthlyStatsCollector;
import aiSafe.reportGeneration.domain.ReportDataCollector;
import aiSafe.reportGeneration.domain.ReportFactory;
import aiSafe.reportGeneration.domain.ReportRenderer;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.simulationreport.repositories.SimulationReportRepository;
import aiSafe.usermanagement.domain.AisafeRoles;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.application.UseCaseController;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for the use case of generating reports. Handles user authorization, transaction management, and delegates the report generation logic to the GenerateReportService.
 * @author 2DE - Group 3
 * @version 2.0
 */
@UseCaseController
public class GenerateReportController {
    /** Factory for creating repositories and managing transactions. */
    private final RepositoryFactory repositoryFactory;
    /** Service that encapsulates the business logic for generating reports. */
    private final AuthorizationService authz;
    /** Service that encapsulates the business logic for generating reports. */
    private final GenerateReportService service;

    public GenerateReportController() {
        this(PersistenceContext.repositories(), AuthzRegistry.authorizationService(), null);
    }

    /** Package-visible constructor for tests (injects service; skips auth when {@code authz} is null). */
    GenerateReportController(final RepositoryFactory repositoryFactory,
                             final GenerateReportService service) {
        this(repositoryFactory, null, service);
    }

    GenerateReportController(final RepositoryFactory repositoryFactory,
                             final AuthorizationService authz,
                             final GenerateReportService service) {
        this.repositoryFactory = Objects.requireNonNull(repositoryFactory);
        this.authz = authz;
        this.service = service != null ? service : buildService(repositoryFactory);
    }

    /**
     * Generates a report based on the specified parameters. This method first checks if the authenticated user has the necessary role to perform this action. It then starts a new transaction, resolves the user's email, and calls the GenerateReportService to generate the report. If the report generation is successful, it commits the transaction and returns the file path of the generated report. If any runtime exception occurs during the process, it rolls back the transaction and rethrows the exception.
     * @param period the month and year for which the report should be generated
     * @param reportType the type of report to generate (e.g., monthly statistics)
     * @param areaCode the code of the air control area for which the report should be generated
     * @param outputDirectory the directory where the generated report file should be saved
     * @return the absolute path of the exported report file
     */
    public String generateReport(final YearMonth period, final ReportType reportType, final String areaCode, final String outputDirectory) {
        if (authz != null) {
            authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.FLIGHT_CONTROL_OPERATOR);
        }

        final TransactionalContext txCtx = repositoryFactory.newTransactionalContext();
        txCtx.beginTransaction();

        try {
            final Email userEmail = resolveUserEmail();
            final String filePath = service.generateReport(period, reportType, areaCode, outputDirectory, userEmail, txCtx);
            txCtx.commit();
            return filePath;
        } catch (final RuntimeException e) {
            txCtx.rollback();
            throw e;
        } finally {
            txCtx.close();
        }
    }

    /**
     * Retrieves the list of air control areas that the authenticated user has access to. This method checks if the user has the necessary role to perform this action and then calls the GenerateReportService to get the list of air control areas based on the user's email.
     * @return an iterable of AirControlArea objects that the user has access to
     */
    public Iterable<AirControlArea> getAirControlArea() {
        if (authz != null) {
            authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.FLIGHT_CONTROL_OPERATOR);
        }
        return service.getAirControlArea(resolveUserEmail());
    }

    /**
     * Resolves the email of the authenticated user. If there is no authenticated user (i.e., authz is null), it returns a default email address. Otherwise, it retrieves the email from the authenticated user's session.
     * @return the email of the authenticated user or a default email if no user is authenticated
     */
    private Email resolveUserEmail() {
        if (authz == null) {
            return new Email("operator@company.com");
        }
        return new Email(authz.session().get().authenticatedUser().email().toString());
    }

    /**
     * Builds the GenerateReportService by creating the necessary repositories, data collectors, report factory, and report renderer. It initializes the SimulationReportRepository and FlightRepository using the provided RepositoryFactory, sets up a map of ReportType to ReportDataCollector (currently only MonthlyStatsCollector), creates a ReportFactory with the collectors, and a CompositeReportRenderer that combines PDF and text rendering capabilities. Finally, it returns a new instance of GenerateReportService with the initialized components.
     * @param repoFactory the RepositoryFactory used to create the necessary repositories for the service
     * @return an instance of GenerateReportService initialized with the required components for report generation
     */
    private static GenerateReportService buildService(final RepositoryFactory repoFactory) {
        final SimulationReportRepository simRepo = repoFactory.simulationReports();
        final FlightRepository flightRepo = repoFactory.flights();

        final Map<ReportType, ReportDataCollector> collectors = new EnumMap<>(ReportType.class);
        collectors.put(ReportType.MONTHLY_STATISTICS, new MonthlyStatsCollector(simRepo, flightRepo));

        final ReportFactory factory = new ReportFactory(collectors);
        final ReportRenderer renderer = new CompositeReportRenderer(new PdfReportRenderer(), new TextReportRenderer());

        return new GenerateReportService(repoFactory, factory, renderer);
    }
}
