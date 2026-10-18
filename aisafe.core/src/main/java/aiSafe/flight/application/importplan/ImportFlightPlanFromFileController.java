package aiSafe.flight.application.importplan;

import aiSafe.airtransportcompany.domain.IataCode;
import aiSafe.flight.domain.FlightDesignator;
import aiSafe.infrastructure.persistence.PersistenceContext;
import aiSafe.infrastructure.persistence.RepositoryFactory;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * US121 – import a Core Flight DSL file into an existing {@link Flight} aggregate.
 * Validates the file via US120 before persistence.
 */
@UseCaseController
public class ImportFlightPlanFromFileController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AuthorizationGuardService authorizationGuardService;
    private final RepositoryFactory factory = PersistenceContext.repositories();
    private final FlightPlanFileReader fileReader = new FlightPlanFileReader();
    private final FlightPlanFormatDetector formatDetector = new FlightPlanFormatDetector();
    private final DslFlightPlanImporter dslImporter = new DslFlightPlanImporter();
    private final ImportFlightPlanService importService =
            new ImportFlightPlanService(factory, authz);

    public ImportFlightPlanFromFileController() {
        authorizationGuardService = new AuthorizationGuardService();
    }

    public ImportFlightPlanResult importFlightPlanFromFile(
            final FlightDesignator flightId,
            final Path filePath) {

        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);

        final FlightPlanFileReader.ReadFileResult file = fileReader.read(filePath);
        if (!file.success()) {
            return ImportFlightPlanResult.failure(file.errors());
        }

        return importContent(flightId, file.content(), file.fileName());
    }

    /**
     * Supports tests and callers that already have file text in memory.
     */
    public ImportFlightPlanResult importFlightPlanFromContent(
            final FlightDesignator flightId,
            final String content,
            final String fileName) {

        authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.PILOT);
        return importContent(flightId, content, fileName);
    }

    private ImportFlightPlanResult importContent(
            final FlightDesignator flightId,
            final String content,
            final String fileName) {

        final FlightPlanFileFormat format = formatDetector.detect(content, fileName);
        if (format == FlightPlanFileFormat.UNKNOWN) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.UNSUPPORTED_FORMAT,
                            "Unsupported flight plan file format.")));
        }

        if (format == FlightPlanFileFormat.JSON) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.UNSUPPORTED_FORMAT,
                            "JSON flight plan format is not supported. Use Core Flight DSL (.flight).")));
        }

        final Optional<DslFlightPlanImporter.ImportStepResult> dslResult = dslImporter.importFromContent(content);
        if (dslResult.isEmpty()) {
            return ImportFlightPlanResult.failure(List.of(
                    new ImportFlightPlanError(
                            ImportFlightPlanErrorCode.DSL_VALIDATION,
                            "Flight plan validation failed.")));
        }

        if (!dslResult.get().success()) {
            return ImportFlightPlanResult.failure(dslResult.get().errors());
        }

        final var dslPlan = dslResult.get().dslPlan();
        return importService.importPlan(flightId, dslPlan, content);
    }

    public FlightDesignator generateFlightDesignator(final String airlineIcaoCode, final int flightNumber, final String operationalSuffix) {
        authorizationGuardService.ensureAuthenticatedPilot();

        final  var repository = factory.getAirTransportCompanyRepository();

        final var existingAirline = repository.findByIataCode(IataCode.valueOf(airlineIcaoCode));
        if(existingAirline.isEmpty()){
            throw new IllegalStateException("No air transport company found with the provided IATA code.");
        }

        return new FlightDesignator(airlineIcaoCode, flightNumber, operationalSuffix);
    }

}
