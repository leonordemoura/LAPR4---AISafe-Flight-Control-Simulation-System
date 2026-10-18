package aiSafe.flight.application.importplan.scomp;

import aiSafe.flight.domain.Flight;
import aiSafe.flight.domain.FlightDesignator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ExportFlightPlanSelectionParser {

    private ExportFlightPlanSelectionParser() {
    }

    public static List<FlightDesignator> parse(final String selection, final List<Flight> flights) {
        if (selection.equalsIgnoreCase("all")) {
            return flights.stream().map(Flight::designator).toList();
        }

        final Set<Integer> indexes = new LinkedHashSet<>();
        for (final String token : selection.split(",")) {
            final String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Invalid selection: empty entry.");
            }
            try {
                indexes.add(Integer.parseInt(trimmed));
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid selection: '" + trimmed + "' is not a number.");
            }
        }

        final List<FlightDesignator> designators = new ArrayList<>();
        for (final int index : indexes) {
            if (index < 1 || index > flights.size()) {
                throw new IllegalArgumentException("Invalid selection: " + index + " is out of range.");
            }
            designators.add(flights.get(index - 1).designator());
        }
        return designators;
    }
}
