package aiSafe.reportGeneration.application;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.aircontrolarea.domain.AreaCode;
import aiSafe.aircontrolarea.repositories.AirControlAreaRepository;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.reportGeneration.domain.Report;
import aiSafe.reportGeneration.domain.ReportFactory;
import aiSafe.reportGeneration.domain.ReportRenderer;
import aiSafe.reportGeneration.domain.ReportType;
import aiSafe.reportGeneration.repositories.ReportRepository;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.repositories.TransactionalContext;

import java.time.YearMonth;

/**
 * Service class that encapsulates the business logic for generating reports. It interacts with repositories to validate input parameters, persist generated reports, and uses a ReportRenderer to export the report to a file. This class is designed to be used by the GenerateReportController, which handles user authorization and transaction management.
 * @author 2DE - Group 3
 * @version 2.0
 */
public class GenerateReportService {
    /** Factory for creating repositories and managing transactions. */
    private final RepositoryFactory repositoryFactory;
    /** Factory for creating Report instances based on the specified report type and parameters. */
    private final ReportFactory reportFactory;
    /** Service responsible for rendering Report instances to files in the specified output directory. */
    private final ReportRenderer reportRenderer;

    /** Constructs the service and initializes its dependencies. Validates that none of the dependencies are null. */
    public GenerateReportService(final RepositoryFactory repositoryFactory, final ReportFactory reportFactory, final ReportRenderer reportRenderer) {
        if (repositoryFactory == null) throw new IllegalArgumentException("RepositoryFactory cannot be null.");
        if (reportFactory == null) throw new IllegalArgumentException("ReportFactory cannot be null.");
        if (reportRenderer == null) throw new IllegalArgumentException("ReportRenderer cannot be null.");

        this.repositoryFactory = repositoryFactory;
        this.reportFactory = reportFactory;
        this.reportRenderer = reportRenderer;
    }

    /**
     * Generates a report based on the specified parameters. Validates that the specified air control area exists, creates a Report instance using the ReportFactory, saves it to the ReportRepository, and then uses the ReportRenderer to export the report to a file in the specified output directory. If any validation fails or if any runtime exception occurs during the process, it throws an appropriate exception with a descriptive message.
     * @param period the month and year for which the report should be generated
     * @param reportType the type of report to generate (e.g., monthly statistics)
     * @param areaCode the code of the air control area for which the report should be generated
     * @param outputDirectory the directory where the generated report file should be saved
     * @param userEmail the email of the user requesting the report generation, used for access control and auditing purposes
     * @param txCtx the transactional context for managing database transactions during the report generation process
     * @return the absolute path of the exported report file
     */
    public String generateReport(final YearMonth period, final ReportType reportType, final String areaCode, final String outputDirectory, final Email userEmail, final TransactionalContext txCtx) {

        final AirControlAreaRepository areaRepo = repositoryFactory.airControlAreas(txCtx);

        final AreaCode area = new AreaCode(areaCode);
        areaRepo.findByAreaCode(area).orElseThrow(() -> new IllegalArgumentException("Air control area not found: " + areaCode));

        final Report report = reportFactory.create(reportType, period, area, userEmail);

        final ReportRepository reportRepo = repositoryFactory.reports(txCtx);
        reportRepo.save(report);

        return reportRenderer.render(report, outputDirectory);
    }

    /**
     * Retrieves the list of air control areas that the specified user has access to. This method is used to populate the options for selecting an air control area when generating a report, ensuring that users can only select areas they are authorized to access.
     * @param userEmail the email of the user for whom to retrieve accessible air control areas, used to filter the areas based on the user's permissions
     * @return an iterable collection of AirControlArea instances that the specified user has access to, which can be used to populate selection options in the report generation UI
     */
    public Iterable<AirControlArea> getAirControlArea(final Email userEmail) {
        return repositoryFactory.airControlAreas().getAirControlAreasBasedOnUser(userEmail);
    }
}
