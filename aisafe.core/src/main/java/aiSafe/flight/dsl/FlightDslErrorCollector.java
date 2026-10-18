package aiSafe.flight.dsl;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FlightDslErrorCollector extends BaseErrorListener {

    private final FlightDslErrorType errorType;
    private final List<FlightDslError> errors = new ArrayList<>();

    FlightDslErrorCollector(final FlightDslErrorType errorType) {
        this.errorType = errorType;
    }

    @Override
    public void syntaxError(
            final Recognizer<?, ?> recognizer,
            final Object offendingSymbol,
            final int line,
            final int charPositionInLine,
            final String msg,
            final RecognitionException e) {
        errors.add(new FlightDslError(
                errorType,
                msg,
                line,
                charPositionInLine + 1));
    }

    List<FlightDslError> errors() {
        return Collections.unmodifiableList(errors);
    }
}
