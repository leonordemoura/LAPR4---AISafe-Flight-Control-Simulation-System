package aiSafe.flight.application;

import java.util.regex.Pattern;

/**
 * Minimal structural checks for pasted/file DSL before US080 persistence.
 */
public final class FlightPlanDslContentGuard {

    private static final Pattern FLIGHT_TYPE =
            Pattern.compile("\\btype\\s+(REGULAR|CHARTER)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern LEG_BLOCK =
            Pattern.compile("\\bleg\\s*\\{", Pattern.CASE_INSENSITIVE);

    private FlightPlanDslContentGuard() {
    }

    public static void ensureMinimumStructure(final String dslContent) {
        if (dslContent == null || dslContent.isBlank()) {
            throw new IllegalArgumentException("DSL content cannot be empty.");
        }
        final String dsl = normalize(dslContent);
        if (!FLIGHT_TYPE.matcher(dsl).find()) {
            throw new IllegalArgumentException(
                    "DSL is incomplete: missing flight type (REGULAR or CHARTER).");
        }
        if (!LEG_BLOCK.matcher(dsl).find()) {
            throw new IllegalArgumentException(
                    "DSL is incomplete: missing at least one leg block.");
        }
        if (!hasBalancedBraces(dsl)) {
            throw new IllegalArgumentException(
                    "DSL is incomplete: unbalanced braces.");
        }
    }

    private static String normalize(final String dsl) {
        return dsl.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private static boolean hasBalancedBraces(final String dsl) {
        int depth = 0;
        for (int i = 0; i < dsl.length(); i++) {
            final char c = dsl.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0;
    }
}
