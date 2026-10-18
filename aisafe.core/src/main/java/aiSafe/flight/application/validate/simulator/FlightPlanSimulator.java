package aiSafe.flight.application.validate.simulator;

import aiSafe.flight.application.importplan.scomp.FlightProfileStep;
import aiSafe.flight.domain.FuelQuantity;
import aiSafe.flight.dsl.model.DslCoordinates;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.flight.dsl.model.DslLeg;
import aiSafe.flight.dsl.model.DslSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * US085 physics-based fuel check using aircraft-specific parameters and DSL trajectory.
 * When {@link SimulationWeatherConditions} is provided, surface temperature and pressure
 * adjust the ISA atmosphere and wind overrides segment-level wind during cruise.
 */
public final class FlightPlanSimulator {

    private static final double P0_PA = 101325.0;
    private static final double T0_K = 288.15;
    private static final double LAPSE_RATE_K_PER_M = 0.0065;
    private static final double R_AIR = 287.058;
    private static final double GRAVITY = 9.80665;
    private static final double KNOTS_TO_METERS_PER_SECOND = 0.514444;
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double MAX_CLIMB_THROTTLE = 0.90;
    private static final double DESCENT_IDLE_THRUST_FRACTION = 0.12;
    private static final double DESCENT_MIN_THRUST_FRACTION = 0.05;
    private static final double DESCENT_GLIDE_METERS_PER_METER = 3.0;
    private static final double TAXI_FUEL_KG = 200.0;

    private final List<FlightProfileStep> climbProfile;
    private final List<FlightProfileStep> descendProfile;
    private final SimulationCalibration calibration;

    public FlightPlanSimulator() {
        this(FlightValidationProfileCatalog.defaults(), SimulationCalibration.OPERATIONAL);
    }

    public FlightPlanSimulator(final SimulationCalibration calibration) {
        this(FlightValidationProfileCatalog.defaults(), calibration);
    }

    FlightPlanSimulator(final FlightValidationProfileCatalog profiles, final SimulationCalibration calibration) {
        this(
                Objects.requireNonNull(profiles).climb(),
                Objects.requireNonNull(profiles).descend(),
                calibration);
    }

    private FlightPlanSimulator(
            final List<FlightProfileStep> climbProfile,
            final List<FlightProfileStep> descendProfile,
            final SimulationCalibration calibration
    ) {
        this.climbProfile = List.copyOf(climbProfile);
        this.descendProfile = List.copyOf(descendProfile);
        this.calibration = calibration == null ? SimulationCalibration.OPERATIONAL : calibration;
    }

    public FlightPlanSimulationResult simulate(
            final DslFlightPlan dslPlan,
            final FuelQuantity operationalFuel,
            final SimulatorModelParameters model
    ) {
        return simulate(dslPlan, operationalFuel, model, Optional.empty());
    }

    public FlightPlanSimulationResult simulate(
            final DslFlightPlan dslPlan,
            final FuelQuantity operationalFuel,
            final SimulatorModelParameters model,
            final Optional<SimulationWeatherConditions> weather
    ) {
        Objects.requireNonNull(dslPlan);
        Objects.requireNonNull(operationalFuel);
        Objects.requireNonNull(model);
        Objects.requireNonNull(weather);

        if (dslPlan.legs().isEmpty()) {
            return FlightPlanSimulationResult.failure("Flight plan has no legs to simulate.");
        }

        final double availableFuelKg;
        try {
            availableFuelKg = FuelQuantityKg.toKg(operationalFuel, model.fuelDensityKgPerL());
        } catch (final IllegalArgumentException ex) {
            return FlightPlanSimulationResult.failure(ex.getMessage());
        }

        final double tripAndTaxiFuelKg = estimateRequiredFuelKg(dslPlan, model, weather);
        final double totalNeededKg = tripAndTaxiFuelKg + model.minFuelAtArrivalKg();

        if (totalNeededKg > availableFuelKg) {
            return FlightPlanSimulationResult.insufficientFuel(availableFuelKg, totalNeededKg);
        }
        return FlightPlanSimulationResult.success(availableFuelKg, tripAndTaxiFuelKg);
    }

    private double estimateRequiredFuelKg(
            final DslFlightPlan dslPlan,
            final SimulatorModelParameters model,
            final Optional<SimulationWeatherConditions> weather
    ) {
        double planningFuelKg = 0;
        for (int iteration = 0; iteration < 3; iteration++) {
            planningFuelKg = simulateTripFuelKg(dslPlan, model, planningFuelKg, weather);
        }
        return planningFuelKg + TAXI_FUEL_KG;
    }

    private double simulateTripFuelKg(
            final DslFlightPlan dslPlan,
            final SimulatorModelParameters model,
            final double planningFuelKg,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final List<DslSegment> segments = flattenSegments(dslPlan);
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Flight plan has no segments to simulate.");
        }

        double consumedKg = 0;
        double remainingPlanningFuelKg = planningFuelKg;
        double currentAltitudeMeters = model.startElevationMeters();

        for (int index = 0; index < segments.size(); index++) {
            final DslSegment segment = segments.get(index);
            final boolean lastSegment = index == segments.size() - 1;
            final double segmentFuel = simulateSegmentInTrip(
                    model,
                    segment,
                    lastSegment,
                    currentAltitudeMeters,
                    remainingPlanningFuelKg,
                    weather);
            consumedKg += segmentFuel;
            remainingPlanningFuelKg = Math.max(0, remainingPlanningFuelKg - segmentFuel);
            currentAltitudeMeters = lastSegment ? model.endElevationMeters() : segment.altitudeMeters();
        }
        return consumedKg;
    }

    private static List<DslSegment> flattenSegments(final DslFlightPlan dslPlan) {
        final List<DslSegment> segments = new ArrayList<>();
        for (final DslLeg leg : dslPlan.legs()) {
            if (leg.segments().isEmpty()) {
                throw new IllegalArgumentException("Flight leg has no segments to simulate.");
            }
            segments.addAll(leg.segments());
        }
        return segments;
    }

    private double simulateSegmentInTrip(
            final SimulatorModelParameters model,
            final DslSegment segment,
            final boolean lastSegment,
            final double currentAltitudeMeters,
            final double fuelRemainingKg,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final DslCoordinates start = segment.start();
        final DslCoordinates end = segment.end();
        final double targetCruiseAlt = segment.altitudeMeters();

        double consumed = 0;
        if (currentAltitudeMeters < targetCruiseAlt - 1.0) {
            consumed += simulateVerticalProfile(
                    model,
                    "climb",
                    climbProfile,
                    currentAltitudeMeters,
                    targetCruiseAlt,
                    fuelRemainingKg - consumed,
                    weather);
        } else if (currentAltitudeMeters > targetCruiseAlt + 1.0) {
            consumed += simulateVerticalProfile(
                    model,
                    "descend",
                    descendProfile,
                    currentAltitudeMeters,
                    targetCruiseAlt,
                    fuelRemainingKg - consumed,
                    weather);
        }

        final double segmentDistanceKm = haversineKm(
                start.latitude(),
                start.longitude(),
                end.latitude(),
                end.longitude());
        double cruiseDistanceKm = segmentDistanceKm;
        if (lastSegment) {
            final double descentDistanceMeters = estimateDescentDistance(
                    targetCruiseAlt,
                    model.endElevationMeters());
            cruiseDistanceKm = Math.max(0, segmentDistanceKm - descentDistanceMeters / 1000.0);
        }

        consumed += simulateCruise(
                model,
                segment,
                targetCruiseAlt,
                cruiseDistanceKm,
                fuelRemainingKg - consumed,
                weather);

        if (lastSegment) {
            consumed += simulateVerticalProfile(
                    model,
                    "descend",
                    descendProfile,
                    targetCruiseAlt,
                    model.endElevationMeters(),
                    fuelRemainingKg - consumed,
                    weather);
        }
        return consumed;
    }

    private static double estimateDescentDistance(final double fromAltitudeMeters, final double toAltitudeMeters) {
        if (fromAltitudeMeters <= toAltitudeMeters) {
            return 0;
        }
        return (fromAltitudeMeters - toAltitudeMeters) * DESCENT_GLIDE_METERS_PER_METER;
    }

    private double simulateCruise(
            final SimulatorModelParameters model,
            final DslSegment segment,
            final double altitudeMeters,
            final double distanceKm,
            final double fuelRemainingKg,
            final Optional<SimulationWeatherConditions> weather
    ) {
        if (distanceKm <= 0) {
            return 0;
        }

        final double speedMps = model.cruiseSpeedKnots() * KNOTS_TO_METERS_PER_SECOND;
        final double courseDeg = calculateCourseDeg(
                segment.start().latitude(),
                segment.start().longitude(),
                segment.end().latitude(),
                segment.end().longitude());
        final WindVector wind = resolveWind(segment, weather);
        final double groundSpeedMps = calculateGroundSpeed(speedMps, courseDeg, wind);
        final double durationSeconds = (distanceKm * 1000.0) / groundSpeedMps;
        return fuelBurnForLevelFlight(model, altitudeMeters, speedMps, fuelRemainingKg, durationSeconds, weather);
    }

    private double simulateVerticalProfile(
            final SimulatorModelParameters model,
            final String mode,
            final List<FlightProfileStep> steps,
            final double fromAltitudeMeters,
            final double toAltitudeMeters,
            final double fuelRemainingKg,
            final Optional<SimulationWeatherConditions> weather
    ) {
        if (steps.isEmpty() || Math.abs(fromAltitudeMeters - toAltitudeMeters) < 1.0) {
            return 0;
        }

        final boolean climbing = toAltitudeMeters > fromAltitudeMeters;
        final List<AltitudeBand> bands = buildAltitudeBands(steps, fromAltitudeMeters, toAltitudeMeters, climbing);
        double consumed = 0;
        for (final AltitudeBand band : bands) {
            if (band.deltaAltitudeMeters() <= 0 || band.rateMetersPerSecond() <= 0) {
                continue;
            }
            final double midpointAltitude = (band.startAltitudeMeters() + band.endAltitudeMeters()) / 2.0;
            final double durationSeconds = band.deltaAltitudeMeters() / band.rateMetersPerSecond();
            consumed += fuelBurnForVerticalFlight(
                    model,
                    mode,
                    midpointAltitude,
                    band.speedKnots(),
                    band.rateMetersPerSecond(),
                    fuelRemainingKg - consumed,
                    durationSeconds,
                    weather);
        }
        return consumed;
    }

    private double fuelBurnForLevelFlight(
            final SimulatorModelParameters model,
            final double altitudeMeters,
            final double speedMps,
            final double fuelRemainingKg,
            final double durationSeconds,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final double dragNewtons = dragAtLevelFlight(model, altitudeMeters, speedMps, fuelRemainingKg, weather);
        return scaledFuelFlow(model, altitudeMeters, dragNewtons, weather) * durationSeconds;
    }

    private double fuelBurnForVerticalFlight(
            final SimulatorModelParameters model,
            final String mode,
            final double altitudeMeters,
            final double speedKnots,
            final double rateMetersPerSecond,
            final double fuelRemainingKg,
            final double durationSeconds,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final double speedMps = Math.max(speedKnots * KNOTS_TO_METERS_PER_SECOND, 80.0);
        final double dragNewtons = dragAtLevelFlight(model, altitudeMeters, speedMps, fuelRemainingKg, weather);
        final double thrustNewtons;
        if ("climb".equals(mode)) {
            final double massKg = aircraftMassKg(model, fuelRemainingKg);
            final double climbThrust = dragNewtons + (massKg * GRAVITY * rateMetersPerSecond / speedMps);
            final double maxThrust = totalThrustAtAltitude(model, altitudeMeters, weather) * MAX_CLIMB_THROTTLE;
            thrustNewtons = Math.min(climbThrust, maxThrust);
        } else {
            final double massKg = aircraftMassKg(model, fuelRemainingKg);
            final double idleThrust = totalThrustAtAltitude(model, altitudeMeters, weather) * DESCENT_MIN_THRUST_FRACTION;
            final double dragLimitedThrust = dragNewtons * DESCENT_IDLE_THRUST_FRACTION;
            final double descentThrust = dragNewtons - (massKg * GRAVITY * rateMetersPerSecond / speedMps);
            thrustNewtons = Math.max(idleThrust, Math.max(dragLimitedThrust, descentThrust));
        }
        return scaledFuelFlow(model, altitudeMeters, thrustNewtons, weather) * durationSeconds;
    }

    private double scaledFuelFlow(
            final SimulatorModelParameters model,
            final double altitudeMeters,
            final double totalThrustNewtons,
            final Optional<SimulationWeatherConditions> weather
    ) {
        return fuelFlowForTotalThrust(model, altitudeMeters, totalThrustNewtons, weather)
                * calibration.fuelFlowFactor();
    }

    private double fuelFlowForTotalThrust(
            final SimulatorModelParameters model,
            final double altitudeMeters,
            final double totalThrustNewtons,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final double seaLevelTotal = model.totalThrustSeaLevelNewtons();
        if (seaLevelTotal <= 0 || totalThrustNewtons <= 0) {
            return 0;
        }

        return model.activeEngines().stream()
                .mapToDouble(engine -> {
                    final double engineShare = engine.thrustSeaLevelNewtons() / seaLevelTotal;
                    final double engineThrust = totalThrustNewtons * engineShare;
                    return engine.tsfcNewtonPerNewtonSecond() * engineThrust;
                })
                .sum();
    }

    private double totalThrustAtAltitude(
            final SimulatorModelParameters model,
            final double altitudeMeters,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final double referenceDensity = seaLevelDensity(weather);
        final double altitudeFactor = Math.pow(airDensity(altitudeMeters, weather) / referenceDensity, 0.7);
        return model.totalThrustSeaLevelNewtons() * altitudeFactor;
    }

    private double dragAtLevelFlight(
            final SimulatorModelParameters model,
            final double altitudeMeters,
            final double speedMps,
            final double fuelRemainingKg,
            final Optional<SimulationWeatherConditions> weather
    ) {
        final double rho = airDensity(altitudeMeters, weather);
        final double massKg = aircraftMassKg(model, fuelRemainingKg);
        final double lift = massKg * GRAVITY;
        final double cl = (2.0 * lift) / (rho * speedMps * speedMps * model.wingAreaM2());
        final double cd = model.dragCoefficientZero()
                + (cl * cl) / (Math.PI * model.aspectRatio() * model.oswaldEfficiency());
        return 0.5 * rho * speedMps * speedMps * model.wingAreaM2() * cd;
    }

    private static double aircraftMassKg(final SimulatorModelParameters model, final double fuelRemainingKg) {
        return model.emptyWeightKg() + model.payloadKg() + Math.max(0, fuelRemainingKg);
    }

    private static double seaLevelTemperatureKelvin(final Optional<SimulationWeatherConditions> weather) {
        return weather.map(SimulationWeatherConditions::seaLevelTemperatureKelvin).orElse(T0_K);
    }

    private static double seaLevelPressurePascals(final Optional<SimulationWeatherConditions> weather) {
        return weather.map(SimulationWeatherConditions::seaLevelPressurePascals).orElse(P0_PA);
    }

    private static double seaLevelDensity(final Optional<SimulationWeatherConditions> weather) {
        final double temperature = seaLevelTemperatureKelvin(weather);
        final double pressure = seaLevelPressurePascals(weather);
        return pressure / (R_AIR * temperature);
    }

    private static double airDensity(final double altitudeMeters, final Optional<SimulationWeatherConditions> weather) {
        final double surfaceTemperature = seaLevelTemperatureKelvin(weather);
        final double surfacePressure = seaLevelPressurePascals(weather);
        final double temperature = surfaceTemperature - LAPSE_RATE_K_PER_M * altitudeMeters;
        if (temperature <= 0) {
            return 0.001;
        }
        final double pressure = surfacePressure * Math.pow(
                1.0 - LAPSE_RATE_K_PER_M * altitudeMeters / surfaceTemperature,
                GRAVITY / (R_AIR * LAPSE_RATE_K_PER_M));
        return pressure / (R_AIR * temperature);
    }

    private static WindVector resolveWind(
            final DslSegment segment,
            final Optional<SimulationWeatherConditions> weather
    ) {
        if (weather.isPresent()) {
            final SimulationWeatherConditions conditions = weather.get();
            return new WindVector(conditions.windDirectionDeg(), conditions.windSpeedMps());
        }
        return new WindVector(
                segment.windDirectionDeg(),
                convertWindToMetersPerSecond(segment.windSpeed(), segment.windUnit()));
    }

    private static double calculateGroundSpeed(
            final double tasMps,
            final double courseDeg,
            final WindVector wind
    ) {
        if (wind.speedMps() <= 0) {
            return tasMps;
        }

        final double windDirRad = Math.toRadians(wind.directionDeg());
        final double courseRad = Math.toRadians(courseDeg);
        final double angleDiff = windDirRad - courseRad;
        final double windCorrection = wind.speedMps() * Math.sin(angleDiff);

        if (Math.abs(windCorrection) >= tasMps) {
            return tasMps * 0.1;
        }

        final double groundSpeed = Math.sqrt(tasMps * tasMps - windCorrection * windCorrection)
                - wind.speedMps() * Math.cos(angleDiff);
        return Math.max(groundSpeed, 10.0);
    }

    private static double convertWindToMetersPerSecond(final double value, final String unit) {
        if (unit == null) {
            return 0;
        }
        return switch (unit.toLowerCase()) {
            case "mps" -> value;
            case "kmh" -> value / 3.6;
            case "kt" -> value * KNOTS_TO_METERS_PER_SECOND;
            default -> 0;
        };
    }

    private static double calculateCourseDeg(
            final double lat1,
            final double lon1,
            final double lat2,
            final double lon2
    ) {
        final double dLon = Math.toRadians(lon2 - lon1);
        final double y = Math.sin(dLon) * Math.cos(Math.toRadians(lat2));
        final double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                - Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
        final double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    private static double haversineKm(
            final double lat1,
            final double lon1,
            final double lat2,
            final double lon2
    ) {
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private static List<AltitudeBand> buildAltitudeBands(
            final List<FlightProfileStep> steps,
            final double fromAltitudeMeters,
            final double toAltitudeMeters,
            final boolean climbing
    ) {
        final List<FlightProfileStep> ordered = new ArrayList<>(steps);
        if (!climbing) {
            ordered.sort((a, b) -> Double.compare(b.altitudeMeters(), a.altitudeMeters()));
        }

        final List<AltitudeBand> bands = new ArrayList<>();
        double current = fromAltitudeMeters;
        for (int i = 0; i < ordered.size(); i++) {
            final FlightProfileStep step = ordered.get(i);
            final double nextTarget = climbing
                    ? Math.min(toAltitudeMeters, nextStepAltitude(ordered, i, climbing))
                    : Math.max(toAltitudeMeters, nextStepAltitude(ordered, i, climbing));

            if (climbing && nextTarget <= current) {
                continue;
            }
            if (!climbing && nextTarget >= current) {
                continue;
            }

            final double delta = Math.abs(nextTarget - current);
            if (delta > 0) {
                bands.add(new AltitudeBand(
                        current,
                        nextTarget,
                        delta,
                        Math.abs(step.rateMetersPerSecond()),
                        step.speedKnots()));
                current = nextTarget;
            }
            if (Math.abs(current - toAltitudeMeters) < 1.0) {
                break;
            }
        }
        return bands;
    }

    private static double nextStepAltitude(
            final List<FlightProfileStep> steps,
            final int index,
            final boolean climbing
    ) {
        if (index + 1 < steps.size()) {
            return steps.get(index + 1).altitudeMeters();
        }
        return climbing
                ? steps.get(index).altitudeMeters() + 1000.0
                : steps.get(index).altitudeMeters() - 1000.0;
    }

    private record WindVector(double directionDeg, double speedMps) {
    }

    private record AltitudeBand(
            double startAltitudeMeters,
            double endAltitudeMeters,
            double deltaAltitudeMeters,
            double rateMetersPerSecond,
            double speedKnots
    ) {
    }
}
