package aiSafe.flight.application.importplan.scomp;

import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.domain.*;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.flightRoute.domain.RouteDesignator;
import aiSafe.flightRoute.domain.RouteName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportFlightPlanSelectionParserTest {

    @Test
    void parseSelection_all_returns_every_flight() {
        final List<Flight> flights = List.of(
                flight("TP", 123),
                flight("TP", 456));

        final List<FlightDesignator> selected = ExportFlightPlanSelectionParser.parse("all", flights);

        assertEquals(2, selected.size());
        assertEquals(FlightDesignator.of("TP", 123, null), selected.get(0));
        assertEquals(FlightDesignator.of("TP", 456, null), selected.get(1));
    }

    @Test
    void parseSelection_commaSeparated_returns_requested_flights() {
        final List<Flight> flights = List.of(
                flight("TP", 123),
                flight("TP", 456),
                flight("TP", 789));

        final List<FlightDesignator> selected = ExportFlightPlanSelectionParser.parse("1,3", flights);

        assertEquals(List.of(
                FlightDesignator.of("TP", 123, null),
                FlightDesignator.of("TP", 789, null)), selected);
    }

    @Test
    void parseSelection_rejects_out_of_range_index() {
        final List<Flight> flights = List.of(flight("TP", 123));

        final IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ExportFlightPlanSelectionParser.parse("2", flights));

        assertTrue(ex.getMessage().contains("out of range"));
    }

    private static Flight flight(final String airline, final int number) {
        return new Flight(
                FlightRoute.valueOf(
                        RouteName.valueOf(airline, number),
                        RouteDesignator.valueOf("OPO", "LIS")),
                FlightDesignator.of(airline, number, null),
                FlightType.REGULAR,
                AircraftRegistration.valueOf("CS-TUA"));
    }
}
