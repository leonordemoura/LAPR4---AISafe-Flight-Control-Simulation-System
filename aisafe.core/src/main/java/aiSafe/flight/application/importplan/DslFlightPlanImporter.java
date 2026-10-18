package aiSafe.flight.application.importplan;

import aiSafe.flight.dsl.FlightDslError;
import aiSafe.flight.dsl.FlightDslValidationResult;
import aiSafe.flight.dsl.FlightDslValidator;
import aiSafe.flight.dsl.model.DslFlightPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class DslFlightPlanImporter {

    Optional<ImportStepResult> importFromContent(final String content) {
        final FlightDslValidationResult validation = FlightDslValidator.validate(content);
        if (!validation.isValid()) {
            return Optional.of(ImportStepResult.failure(mapDslErrors(validation)));
        }

        final DslFlightPlan dslPlan = validation.model().orElseThrow();
        return Optional.of(ImportStepResult.success(dslPlan));
    }

    private static List<ImportFlightPlanError> mapDslErrors(final FlightDslValidationResult validation) {
        final List<ImportFlightPlanError> errors = new ArrayList<>();
        for (final FlightDslError error : validation.errors()) {
            errors.add(new ImportFlightPlanError(
                    ImportFlightPlanErrorCode.DSL_VALIDATION,
                    error.message(),
                    error.line(),
                    error.column()));
        }
        return errors;
    }

    record ImportStepResult(boolean success, DslFlightPlan dslPlan, List<ImportFlightPlanError> errors) {

        static ImportStepResult success(final DslFlightPlan dslPlan) {
            return new ImportStepResult(true, dslPlan, List.of());
        }

        static ImportStepResult failure(final List<ImportFlightPlanError> errors) {
            return new ImportStepResult(false, null, errors);
        }
    }
}
