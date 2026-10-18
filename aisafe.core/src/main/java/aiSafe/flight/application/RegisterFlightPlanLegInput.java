package aiSafe.flight.application;

import java.util.Collections;
import java.util.List;

public class RegisterFlightPlanLegInput {

    public final String departureAirport;
    public final String departureDate;
    public final String departureTime;
    public final String arrivalAirport;
    public final String arrivalDate;
    public final String arrivalTime;
    public final String routeOrigin;
    public final String routeDestination;
    public final double legFuel;
    public final String legFuelUnit;
    public final List<RegisterFlightPlanSegmentInput> segments;

    public RegisterFlightPlanLegInput(
            final String departureAirport,
            final String departureDate,
            final String departureTime,
            final String arrivalAirport,
            final String arrivalDate,
            final String arrivalTime,
            final String routeOrigin,
            final String routeDestination,
            final double legFuel,
            final String legFuelUnit,
            final List<RegisterFlightPlanSegmentInput> segments
    ) {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Each leg must have at least one segment.");
        }
        this.departureAirport = departureAirport;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.arrivalAirport = arrivalAirport;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.routeOrigin = routeOrigin;
        this.routeDestination = routeDestination;
        this.legFuel = legFuel;
        this.legFuelUnit = legFuelUnit;
        this.segments = List.copyOf(segments);
    }

    public List<RegisterFlightPlanSegmentInput> segments() {
        return Collections.unmodifiableList(segments);
    }
}
