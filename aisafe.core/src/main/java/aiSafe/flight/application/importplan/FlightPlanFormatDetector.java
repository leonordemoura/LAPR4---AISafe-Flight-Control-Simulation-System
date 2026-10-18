package aiSafe.flight.application.importplan;

final class FlightPlanFormatDetector {

    FlightPlanFileFormat detect(final String content, final String fileName) {
        if (content == null || content.isBlank()) {
            return FlightPlanFileFormat.UNKNOWN;
        }

        final String trimmed = content.stripLeading();
        if (trimmed.startsWith("{") && looksLikeJsonFlightDocument(trimmed)) {
            return FlightPlanFileFormat.JSON;
        }

        if (fileName != null && fileName.toLowerCase().endsWith(".flight")) {
            return FlightPlanFileFormat.CORE_FLIGHT_DSL;
        }

        if (trimmed.regionMatches(true, 0, "flight", 0, "flight".length())) {
            return FlightPlanFileFormat.CORE_FLIGHT_DSL;
        }

        return FlightPlanFileFormat.UNKNOWN;
    }

    private static boolean looksLikeJsonFlightDocument(final String content) {
        return content.contains("\"Leg\"")
                || content.contains("\"Departure\"")
                || content.contains("\"Flight Profile\"");
    }
}
