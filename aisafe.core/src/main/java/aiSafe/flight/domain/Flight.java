package aiSafe.flight.domain;

import aiSafe.PilotCertification.domain.PilotID;
import aiSafe.aircraft.domain.AircraftRegistration;
import aiSafe.flightRoute.domain.FlightRoute;
import aiSafe.weatherdata.domain.WeatherData;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "flight")
public class Flight implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "routeID", referencedColumnName = "route_id", nullable = false)
    private FlightRoute route;

    @Embedded
    private FlightDesignator designator;

    @Enumerated(EnumType.STRING)
    private FlightType type;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlightPlan> flightPlans = new ArrayList<>();

    @Embedded
    private AircraftRegistration aircraftRegistration;

    protected Flight() {}

    public Flight(
            final FlightRoute route,
            final FlightDesignator designator,
            final FlightType type,
            final AircraftRegistration aircraftRegistration
    ) {
        Preconditions.noneNull(route, designator, type, aircraftRegistration);

        this.route = route;
        this.designator = designator;
        this.type = type;
        this.aircraftRegistration = aircraftRegistration;
    }

    /**
     * Creates a new DRAFT plan, or replaces the current DRAFT by cancelling it (history kept as CANCELLED).
     * Blocks when a VALIDATED plan already exists for this flight.
     */
    public FlightPlan registerOrReplaceFlightPlan(
            final AircraftRegistration aircraftRegistration,
            final PilotID pilotId,
            final DepartureDateTime departureDateTime,
            final ArrivalDateTime arrivalDateTime,
            final FuelQuantity fuelQuantity,
            final DslSource dslSource
    ) {
        ensureNoLockedCurrentPlanForRegistration();
        cancelCurrentDraftIfPresent();
        return addFlightPlan(
                aircraftRegistration,
                pilotId,
                departureDateTime,
                arrivalDateTime,
                fuelQuantity,
                dslSource);
    }

    public FlightPlan addFlightPlan(
            final AircraftRegistration aircraftRegistration,
            final PilotID pilotId,
            final DepartureDateTime departureDateTime,
            final ArrivalDateTime arrivalDateTime,
            final FuelQuantity fuelQuantity,
            final DslSource dslSource
    ) {
        if (aircraftRegistration == null) {
            throw new IllegalArgumentException("Aircraft registration is required.");
        }
        if (pilotId == null) {
            throw new IllegalArgumentException("Pilot id is required.");
        }
        if (departureDateTime == null) {
            throw new IllegalArgumentException("Departure date/time is required.");
        }
        if (arrivalDateTime == null) {
            throw new IllegalArgumentException("Arrival date/time is required.");
        }
        if (fuelQuantity == null) {
            throw new IllegalArgumentException("Fuel quantity is required.");
        }
        if (dslSource == null) {
            throw new IllegalArgumentException("DSL source is required.");
        }

        final FlightPlan plan = new FlightPlan(
                this,
                aircraftRegistration,
                pilotId,
                departureDateTime,
                arrivalDateTime,
                fuelQuantity,
                dslSource);
        flightPlans.add(plan);
        return plan;
    }

    /**
     * Id of the current DRAFT plan on this flight (for pilot schedule exclusion when replacing).
     */
    public Optional<Long> pendingReplaceablePlanId() {
        return currentFlightPlanOptional()
                .filter(FlightPlan::isDraft)
                .map(FlightPlan::planId);
    }

    /**
     * The operational flight plan: DRAFT or VALIDATED (most recent if legacy data has more than one).
     */
    public Optional<FlightPlan> currentFlightPlanOptional() {
        for (int i = flightPlans.size() - 1; i >= 0; i--) {
            final FlightPlan plan = flightPlans.get(i);
            if (plan.status() == FlightPlanStatus.CANCELLED) {
                continue;
            }
            if (plan.isPending()) {
                return Optional.of(plan);
            }
        }
        return Optional.empty();
    }

    public FlightRoute route() {
        return route;
    }

    public FlightDesignator designator() {
        return designator;
    }

    public FlightType type() {
        return type;
    }

    public List<FlightPlan> flightPlans() {
        return Collections.unmodifiableList(flightPlans);
    }

    /**
     * Returns the current operational flight plan, if any.
     */
    public FlightPlan flightPlan() {
        return currentFlightPlanOptional().orElse(null);
    }

    public AircraftRegistration aircraftRegistration() {
        return aircraftRegistration;
    }

    public boolean isActiveOrPending() {
        return currentFlightPlanOptional().map(FlightPlan::isPending).orElse(false);
    }

    /**
     * Attaches an imported plan: replaces an existing DRAFT (cancelled for history) or creates the first plan.
     */
    public void importFlightPlan(final FlightPlan plan) {
        Preconditions.noneNull(plan, "Flight plan is required.");

        if (plan.dslSource() == null) {
            throw new IllegalArgumentException("Imported flight plan must include DSL content.");
        }

        ensureNoLockedCurrentPlanForRegistration();
        cancelCurrentDraftIfPresent();

        plan.attachToFlight(this);
        flightPlans.add(plan);

        validateCurrentPlanRoute();
    }

    private void ensureNoLockedCurrentPlanForRegistration() {
        final Optional<FlightPlan> current = currentFlightPlanOptional();
        if (current.isEmpty()) {
            return;
        }
        final FlightPlanStatus status = current.get().status();
        if (status == FlightPlanStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Cannot register or import a new flight plan while the current plan is VALIDATED.");
        }
    }

    private void cancelCurrentDraftIfPresent() {
        currentFlightPlanOptional()
                .filter(FlightPlan::isDraft)
                .ifPresent(FlightPlan::cancel);
    }

    private void validateCurrentPlanRoute() {
        final FlightPlan current = flightPlan();
        if (current != null && current.routeSignature() != null
                && !route.routeDesignator().toString().equals(current.routeSignature())) {
            throw new IllegalArgumentException(
                    "Flight plan route (" + current.routeSignature()
                            + ") does not match flight route (" + route.routeDesignator().toString() + ").");
        }
    }

    /**
     * Returns true if any non-cancelled plan departs on a calendar day strictly after the deactivation day.
     */
    public boolean hasPlanDepartingAfter(final LocalDateTime deactivationDate) {
        if (deactivationDate == null) {
            throw new IllegalArgumentException("Deactivation date must not be null.");
        }
        final LocalDate cutoff = deactivationDate.toLocalDate();
        return flightPlans.stream()
                .filter(plan -> plan.status() != FlightPlanStatus.CANCELLED)
                .anyMatch(plan -> plan.departureDateTime().value().toLocalDate().isAfter(cutoff));
    }

    public void insertWeatherData(final WeatherData weatherData) {
        Preconditions.noneNull(weatherData, "Weather data is required.");

        final FlightPlan target = currentFlightPlanOptional()
                .filter(FlightPlan::isPending)
                .orElseThrow(() -> new IllegalStateException(
                        "No pending flight plan found for flight " + designator + "."));

        target.insertWeatherData(weatherData);
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Flight that)) return false;
        return this.designator.equals(that.designator);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Flight that)) return false;
        return this.designator.equals(that.designator);
    }

    @Override
    public int hashCode() {
        return designator.hashCode();
    }

    @Override
    public String toString() {
        final FlightPlan current = flightPlan();
        final String planStatus = current == null ? "none" : current.status().toString();
        return String.format("Flight[%s | %s | %s]", designator, type, planStatus);
    }
}
