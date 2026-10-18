package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flight.dsl.model.DslFlightPlan;
import aiSafe.weatherdata.domain.WeatherData;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "flight_plan")
public class FlightPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "flight_plan_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightPlanStatus status;

    @Embedded
    private AircraftRegistration aircraftRegistration;

    @Embedded
    private PilotID pilotId;

    @Embedded
    private DepartureDateTime departureDateTime;

    @Embedded
    private ArrivalDateTime arrivalDateTime;

    @Embedded
    private FuelQuantity fuelQuantity;

    @Embedded
    private DslSource dslSource;

    @ManyToOne
    @JoinColumn(name = "weather_data_id")
    private WeatherData weatherData;

    @Column(name = "plan_dsl_identifier")
    private String dslIdentifier;

    @Column(name = "plan_origin_airport")
    private String originAirport;

    @Column(name = "plan_destination_airport")
    private String destinationAirport;

    @Column(name = "plan_route_signature")
    private String routeSignature;

    @Column(name = "plan_leg_count")
    private Integer legCount;

    protected FlightPlan() {}

    FlightPlan(
            final Flight flight,
            final AircraftRegistration aircraftRegistration,
            final PilotID pilotId,
            final DepartureDateTime departureDateTime,
            final ArrivalDateTime arrivalDateTime,
            final FuelQuantity fuelQuantity,
            final DslSource dslSource
    ) {
        this.flight = Objects.requireNonNull(flight, "Flight is required.");
        this.aircraftRegistration = Objects.requireNonNull(aircraftRegistration, "Aircraft registration is required.");
        this.pilotId = Objects.requireNonNull(pilotId, "Pilot id is required.");
        this.departureDateTime = Objects.requireNonNull(departureDateTime, "Departure date/time is required.");
        this.arrivalDateTime = Objects.requireNonNull(arrivalDateTime, "Arrival date/time is required.");
        this.fuelQuantity = Objects.requireNonNull(fuelQuantity, "Fuel quantity is required.");
        this.dslSource = Objects.requireNonNull(dslSource, "DSL source is required.");
        this.status = FlightPlanStatus.DRAFT;
        deriveRouteMetadataFromDsl(dslSource.content());
    }

    /**
     * Fills summary fields not derived from incomplete pasted DSL (US080 console paste / file path).
     *
     * @param flightDesignatorFallback designator from the registered {@link Flight} when DSL has no {@code flight} line
     * @param fallbackOrigin           first-leg departure airport when DSL has no {@code departure} lines
     * @param fallbackDestination      last-leg arrival airport when DSL has no {@code arrival} lines
     * @param fallbackLegCount         leg count from manual UI input when DSL has no {@code leg} blocks
     */
    public void applyRegistrationMetadata(
            final String flightDesignatorFallback,
            final String fallbackOrigin,
            final String fallbackDestination,
            final Integer fallbackLegCount
    ) {
        if ((dslIdentifier == null || dslIdentifier.isBlank())
                && flightDesignatorFallback != null
                && !flightDesignatorFallback.isBlank()) {
            this.dslIdentifier = flightDesignatorFallback.trim();
        }
        if (originAirport == null && fallbackOrigin != null && !fallbackOrigin.isBlank()) {
            this.originAirport = fallbackOrigin.trim().toUpperCase();
        }
        if (destinationAirport == null && fallbackDestination != null && !fallbackDestination.isBlank()) {
            this.destinationAirport = fallbackDestination.trim().toUpperCase();
        }
        if (originAirport != null && destinationAirport != null) {
            this.routeSignature = originAirport + "-" + destinationAirport;
        }
        if ((legCount == null || legCount == 0)
                && fallbackLegCount != null
                && fallbackLegCount > 0) {
            this.legCount = fallbackLegCount;
        }
    }

    private void deriveRouteMetadataFromDsl(final String dslContent) {
        final String identifier = extractFlightIdentifier(dslContent);
        if (identifier != null) {
            this.dslIdentifier = identifier;
        }
        final String[] airports = extractFirstDepartureAndLastArrivalAirports(dslContent);
        if (airports[0] != null && airports[1] != null) {
            this.originAirport = airports[0];
            this.destinationAirport = airports[1];
            this.routeSignature = airports[0] + "-" + airports[1];
        }
        final int legs = countLegBlocks(dslContent);
        if (legs > 0) {
            this.legCount = legs;
        }
    }

    /**
     * Reads {@code flight <identifier> \{} from DSL text (case-insensitive), or {@code null} if absent.
     */
    public static String readDslFlightIdentifier(final String dslContent) {
        return extractFlightIdentifier(normalizeDslText(dslContent));
    }

    private static String extractFlightIdentifier(final String dsl) {
        final java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("flight\\s+(\\S+)\\s*\\{", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(dsl);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** First {@code departure} airport and last {@code arrival} airport across all legs. */
    static String[] extractFirstDepartureAndLastArrivalAirports(final String dsl) {
        final String normalized = normalizeDslText(dsl);
        String firstDeparture = null;
        final java.util.regex.Matcher depMatcher = java.util.regex.Pattern
                .compile("departure\\s+([A-Z]{3,4})\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(normalized);
        if (depMatcher.find()) {
            firstDeparture = depMatcher.group(1).toUpperCase();
        }
        String lastArrival = null;
        final java.util.regex.Matcher arrMatcher = java.util.regex.Pattern
                .compile("arrival\\s+([A-Z]{3,4})\\b", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(normalized);
        while (arrMatcher.find()) {
            lastArrival = arrMatcher.group(1).toUpperCase();
        }
        return new String[]{firstDeparture, lastArrival};
    }

    private static String normalizeDslText(final String dsl) {
        if (dsl == null) {
            return "";
        }
        return dsl.replace("\uFEFF", "")
                .replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private static int countLegBlocks(final String dsl) {
        final java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\\bleg\\s*\\{", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(normalizeDslText(dsl));
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    public static FlightPlan fromValidatedImport(final DslFlightPlan dslPlan) {
        Objects.requireNonNull(dslPlan);
        if (dslPlan.legs().isEmpty()) {
            throw new IllegalArgumentException("Flight plan must contain at least one leg.");
        }

        final FlightPlan fp = new FlightPlan();
        fp.status = FlightPlanStatus.DRAFT;
        fp.dslIdentifier = dslPlan.identifier();
        fp.originAirport = dslPlan.legs().getFirst().departure().airportCode();
        fp.destinationAirport = dslPlan.legs().getLast().arrival().airportCode();
        fp.routeSignature = fp.originAirport + "-" + fp.destinationAirport;
        fp.legCount = dslPlan.legs().size();
        return fp;
    }

    public static FlightPlan fromDslImport(final DslFlightPlan dslPlan, final String rawDslContent) {
        final FlightPlan plan = fromValidatedImport(dslPlan);
        plan.dslSource = new DslSource(rawDslContent);
        return plan;
    }

    public static FlightPlan fromDslImport(
            final DslFlightPlan dslPlan,
            final String rawDslContent,
            final AircraftRegistration aircraftRegistration,
            final PilotID pilotId,
            final DepartureDateTime departureDateTime,
            final ArrivalDateTime arrivalDateTime,
            final FuelQuantity fuelQuantity
    ) {
        final FlightPlan plan = fromDslImport(dslPlan, rawDslContent);
        plan.enrichOperationalData(
                aircraftRegistration, pilotId, departureDateTime, arrivalDateTime, fuelQuantity);
        return plan;
    }

    void attachToFlight(final Flight owner) {
        this.flight = Objects.requireNonNull(owner, "Flight is required.");
    }

    void enrichOperationalData(
            final AircraftRegistration aircraftRegistration,
            final PilotID pilotId,
            final DepartureDateTime departureDateTime,
            final ArrivalDateTime arrivalDateTime,
            final FuelQuantity fuelQuantity
    ) {
        if (aircraftRegistration != null) {
            this.aircraftRegistration = aircraftRegistration;
        }
        if (pilotId != null) {
            this.pilotId = pilotId;
        }
        if (departureDateTime != null) {
            this.departureDateTime = departureDateTime;
        }
        if (arrivalDateTime != null) {
            this.arrivalDateTime = arrivalDateTime;
        }
        if (fuelQuantity != null) {
            this.fuelQuantity = fuelQuantity;
        }
    }

    void applyImportFrom(final FlightPlan imported) {
        Objects.requireNonNull(imported);
        this.dslIdentifier = imported.dslIdentifier;
        this.originAirport = imported.originAirport;
        this.destinationAirport = imported.destinationAirport;
        this.routeSignature = imported.routeSignature;
        this.legCount = imported.legCount;
        if (imported.dslSource != null) {
            this.dslSource = imported.dslSource;
        }
        enrichOperationalData(
                imported.aircraftRegistration,
                imported.pilotId,
                imported.departureDateTime,
                imported.arrivalDateTime,
                imported.fuelQuantity);
    }

    public Long planId() {
        return id;
    }

    public FlightPlanStatus status() {
        return status;
    }

    public AircraftRegistration aircraftRegistration() {
        return aircraftRegistration;
    }

    public PilotID pilotId() {
        return pilotId;
    }

    public DepartureDateTime departureDateTime() {
        return departureDateTime;
    }

    public ArrivalDateTime arrivalDateTime() {
        return arrivalDateTime;
    }

    public FuelQuantity fuelQuantity() {
        return fuelQuantity;
    }

    public DslSource dslSource() {
        return dslSource;
    }

    public String dslIdentifier() {
        return dslIdentifier;
    }

    public String originAirport() {
        return originAirport;
    }

    public String destinationAirport() {
        return destinationAirport;
    }

    public String routeSignature() {
        return routeSignature;
    }

    public int legCount() {
        return legCount == null ? 0 : legCount;
    }

    public boolean isDraft() {
        return status == FlightPlanStatus.DRAFT;
    }

    public boolean isValidated() {
        return status == FlightPlanStatus.VALIDATED;
    }

    public boolean isPending() {
        return status == FlightPlanStatus.DRAFT || status == FlightPlanStatus.VALIDATED;
    }

    /**
     * Marks a draft plan as cancelled (e.g. superseded by a new draft).
     */
    public void cancel() {
        if (status == FlightPlanStatus.CANCELLED) {
            throw new IllegalStateException("Flight plan is already cancelled.");
        }
        if (status == FlightPlanStatus.VALIDATED) {
            throw new IllegalStateException("Cannot cancel a validated flight plan.");
        }
        this.status = FlightPlanStatus.CANCELLED;
    }

    /**
     * Promotes a DRAFT plan to VALIDATED after full DSL validation (US085).
     */
    public void markAsValidated() {
        if (status == FlightPlanStatus.CANCELLED) {
            throw new IllegalStateException("Cannot validate a cancelled flight plan.");
        }
        if (status == FlightPlanStatus.VALIDATED) {
            throw new IllegalStateException("Flight plan is already validated.");
        }
        if (status != FlightPlanStatus.DRAFT) {
            throw new IllegalStateException("Only draft flight plans can be validated.");
        }
        this.status = FlightPlanStatus.VALIDATED;
    }

    public void insertWeatherData(final WeatherData weatherData) {
        Objects.requireNonNull(weatherData, "Weather data is required.");

        if (status == FlightPlanStatus.CANCELLED) {
            throw new IllegalStateException("Cannot insert weather data into a cancelled flight plan.");
        }

        this.weatherData = weatherData;

        // If the plan had been tested/validated, the test is now void.
        if (status == FlightPlanStatus.VALIDATED) {
            this.status = FlightPlanStatus.DRAFT;
        }
    }

    public WeatherData weatherData() {
        return weatherData;
    }

    public boolean hasWeatherData() {
        return weatherData != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightPlan that)) return false;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return status == that.status
                && Objects.equals(aircraftRegistration, that.aircraftRegistration)
                && Objects.equals(pilotId, that.pilotId)
                && Objects.equals(departureDateTime, that.departureDateTime)
                && Objects.equals(fuelQuantity, that.fuelQuantity)
                && Objects.equals(dslIdentifier, that.dslIdentifier);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(status, aircraftRegistration, pilotId);
    }

    @Override
    public String toString() {
        if (routeSignature != null) {
            return "FlightPlan[" + status + ", " + routeSignature + ", legs=" + legCount() + "]";
        }
        return "FlightPlan[" + status + ", aircraft=" + aircraftRegistration + ", pilot=" + pilotId + "]";
    }
}
