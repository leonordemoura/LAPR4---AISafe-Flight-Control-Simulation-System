package aiSafe.flight.application;

import java.util.Locale;
import java.util.Set;

public class RegisterFlightPlanSegmentInput {

    private static final Set<String> ALLOWED_WIND_UNITS = Set.of("mps", "kmh", "kt");

    public final double startLatitude;
    public final double startLongitude;
    public final double endLatitude;
    public final double endLongitude;
    public final double altitudeMeters;
    public final double widthKm;
    public final double windDirectionDeg;
    public final double windSpeed;
    public final String windUnit;

    public RegisterFlightPlanSegmentInput(
            final double startLatitude,
            final double startLongitude,
            final double endLatitude,
            final double endLongitude,
            final double altitudeMeters,
            final double widthKm,
            final double windDirectionDeg,
            final double windSpeed,
            final String windUnit
    ) {
        if (altitudeMeters <= 0) {
            throw new IllegalArgumentException("Altitude must be positive.");
        }
        if (widthKm <= 0) {
            throw new IllegalArgumentException("Segment width must be positive.");
        }
        if (windDirectionDeg < 0 || windDirectionDeg >= 360) {
            throw new IllegalArgumentException("Wind direction must be between 0 and 359 degrees.");
        }
        if (windSpeed < 0) {
            throw new IllegalArgumentException("Wind speed must be non-negative.");
        }
        final String normalizedWindUnit = normalizeWindUnit(windUnit);

        this.startLatitude = startLatitude;
        this.startLongitude = startLongitude;
        this.endLatitude = endLatitude;
        this.endLongitude = endLongitude;
        this.altitudeMeters = altitudeMeters;
        this.widthKm = widthKm;
        this.windDirectionDeg = windDirectionDeg;
        this.windSpeed = windSpeed;
        this.windUnit = normalizedWindUnit;
    }

    private static String normalizeWindUnit(final String unit) {
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Wind unit is required (mps, kmh, or kt).");
        }
        final String normalized = unit.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_WIND_UNITS.contains(normalized)) {
            throw new IllegalArgumentException("Wind unit must be mps, kmh, or kt.");
        }
        return normalized;
    }
}
