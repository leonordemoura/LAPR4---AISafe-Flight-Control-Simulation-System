package aiSafe.flight.dsl;

import aiSafe.flight.dsl.model.DslFlightPlan;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for US083 / US081: validates Core Flight DSL text and builds the intermediate model.
 */
public final class FlightDslValidator {

    private FlightDslValidator() {}

    public static FlightDslValidationResult validate(final String source) {
        if (source == null || source.isBlank()) {
            return FlightDslValidationResult.failure(List.of(
                    new FlightDslError(
                            FlightDslErrorType.LEXICAL,
                            "Flight plan input is empty.",
                            1,
                            1)));
        }

        final var input = CharStreams.fromString(source);
        final var lexer = new FlightDSLLexer(input);
        lexer.removeErrorListeners();
        final var lexerErrors = new FlightDslErrorCollector(FlightDslErrorType.LEXICAL);
        lexer.addErrorListener(lexerErrors);

        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FlightDSLParser(tokens);
        parser.removeErrorListeners();
        final var parserErrors = new FlightDslErrorCollector(FlightDslErrorType.SYNTAX);
        parser.addErrorListener(parserErrors);

        final var structureListener = new FlightDslStructureListener();
        parser.addParseListener(structureListener);

        final var tree = parser.flightPlan();

        final List<FlightDslError> errors = new ArrayList<>();
        errors.addAll(lexerErrors.errors());
        errors.addAll(parserErrors.errors());

        if (!errors.isEmpty()) {
            return FlightDslValidationResult.failure(errors);
        }

        final DslFlightPlan model = new FlightDslModelBuilder().visitFlightPlan(tree);

        if (structureListener.legCount() != model.legs().size()) {
            return FlightDslValidationResult.failure(List.of(
                    new FlightDslError(
                            FlightDslErrorType.SEMANTIC,
                            "Internal parse structure mismatch for legs.",
                            1,
                            1)));
        }

        final List<FlightDslError> semanticErrors = FlightDslSemanticValidator.validate(model);
        if (!semanticErrors.isEmpty()) {
            return FlightDslValidationResult.failure(semanticErrors);
        }

        return FlightDslValidationResult.success(model);
    }

    /**
     * Parses and validates lexical/syntax structure only (US083 without semantic rules).
     * Used by US081 file import when business rules are deferred to US085.
     */
    public static FlightDslValidationResult validateWithoutSemantic(final String source) {
        if (source == null || source.isBlank()) {
            return FlightDslValidationResult.failure(List.of(
                    new FlightDslError(
                            FlightDslErrorType.LEXICAL,
                            "Flight plan input is empty.",
                            1,
                            1)));
        }

        final var input = CharStreams.fromString(source);
        final var lexer = new FlightDSLLexer(input);
        lexer.removeErrorListeners();
        final var lexerErrors = new FlightDslErrorCollector(FlightDslErrorType.LEXICAL);
        lexer.addErrorListener(lexerErrors);

        final var tokens = new CommonTokenStream(lexer);
        final var parser = new FlightDSLParser(tokens);
        parser.removeErrorListeners();
        final var parserErrors = new FlightDslErrorCollector(FlightDslErrorType.SYNTAX);
        parser.addErrorListener(parserErrors);

        final var structureListener = new FlightDslStructureListener();
        parser.addParseListener(structureListener);

        final var tree = parser.flightPlan();

        final List<FlightDslError> errors = new ArrayList<>();
        errors.addAll(lexerErrors.errors());
        errors.addAll(parserErrors.errors());

        if (!errors.isEmpty()) {
            return FlightDslValidationResult.failure(errors);
        }

        final DslFlightPlan model = new FlightDslModelBuilder().visitFlightPlan(tree);

        if (structureListener.legCount() != model.legs().size()) {
            return FlightDslValidationResult.failure(List.of(
                    new FlightDslError(
                            FlightDslErrorType.SEMANTIC,
                            "Internal parse structure mismatch for legs.",
                            1,
                            1)));
        }

        return FlightDslValidationResult.success(model);
    }

    /**
     * Validates DSL text read from a file (UTF-8). Intended for US081 file import.
     */
    public static FlightDslValidationResult validateFile(final Path path) throws IOException {
        return validate(Files.readString(path, StandardCharsets.UTF_8));
    }
}
