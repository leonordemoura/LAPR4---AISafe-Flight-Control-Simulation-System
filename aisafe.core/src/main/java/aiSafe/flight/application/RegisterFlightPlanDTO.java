package aiSafe.flight.application;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class RegisterFlightPlanDTO {

    public final String flightAirlineCode;
    public final int flightNumber;
    public final String flightOperationalSuffix;
    /** Pilot business id (matches {@code PilotCertification} / {@code PilotID}), not login email at input. */
    public final String pilotId;
    public final LocalDateTime departureDateTime;
    public final LocalDateTime arrivalDateTime;
    public final double fuelQuantity;
    public final String fuelUnit;
    /** Raw DSL text when provided directly (paste/file). */
    public final String dslContent;
    /** Legs for UI-driven DSL generation; used when dslContent is blank. */
    public final List<RegisterFlightPlanLegInput> legs;
    /** REGULAR or CHARTER — used when generating DSL from legs (same as domain {@link aiSafe.flight.domain.FlightType}). */
    public final String flightTypeForDsl;

    public RegisterFlightPlanDTO(
            final String flightAirlineCode,
            final int flightNumber,
            final String flightOperationalSuffix,
            final String pilotId,
            final LocalDateTime departureDateTime,
            final LocalDateTime arrivalDateTime,
            final double fuelQuantity,
            final String fuelUnit,
            final String dslContent,
            final List<RegisterFlightPlanLegInput> legs,
            final String flightTypeForDsl
    ) {
        this.flightAirlineCode = flightAirlineCode;
        this.flightNumber = flightNumber;
        this.flightOperationalSuffix = flightOperationalSuffix;
        this.pilotId = pilotId;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.fuelQuantity = fuelQuantity;
        this.fuelUnit = fuelUnit;
        this.dslContent = dslContent;
        this.legs = legs == null ? List.of() : List.copyOf(legs);
        this.flightTypeForDsl = flightTypeForDsl;
    }

    public List<RegisterFlightPlanLegInput> legs() {
        return Collections.unmodifiableList(legs);
    }

    public String flightDesignatorText() {
        final String suffix = flightOperationalSuffix == null || flightOperationalSuffix.isBlank()
                ? ""
                : flightOperationalSuffix.trim();
        return flightAirlineCode + flightNumber + suffix;
    }
}
