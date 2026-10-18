package aiSafe.aircraft.domain;

import aiSafe.aircraftModel.domain.AircraftModel;
import aiSafe.aircraftmanagement.domain.Country;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "aircraft")
public class Aircraft implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "registration", nullable = false, unique = true))
    private AircraftRegistration registration;

    @ManyToOne(optional = false)
    @JoinColumns({
            @JoinColumn(
                    name = "aircraft_model_id",
                    referencedColumnName = "aircraft_model_id"
            )
    })
    private AircraftModel aircraftModel;

    @ElementCollection
    @CollectionTable(
            name = "aircraft_engines",
            joinColumns = @JoinColumn(name = "aircraft_id")
    )
    private List<CurrentEngine> engineModels = new ArrayList<>();

    @Embedded
    private CabinConfiguration cabinConfiguration;

    @Embedded
    private RegistrationYear registrationYear;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "country_name", nullable = false)),
            @AttributeOverride(name = "numericCode", column = @Column(name = "country_numeric_code", nullable = false))
    })
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "companyID", referencedColumnName = "companyID", nullable = false)
    private AirTransportCompany company;

    protected Aircraft() {
    }

    public Aircraft(
            final AircraftRegistration registration,
            final AircraftModel aircraftModel,
            final List<CurrentEngine> engineModels,
            final CabinConfiguration cabinConfiguration,
            final RegistrationYear registrationYear,
            final Country country,
            final AircraftStatus status,
            final AirTransportCompany company
    ) {

        if (registration == null || aircraftModel == null || engineModels == null ||
                cabinConfiguration == null || registrationYear == null ||
                country == null || status == null || company == null) {
            throw new IllegalArgumentException("Mandatory fields missing");
        }

        if (engineModels.isEmpty()) {
            throw new IllegalArgumentException("Aircraft must have at least one engine");
        }

        if (cabinConfiguration.totalSeats() > aircraftModel.maximumSeats().seats()) {
            throw new IllegalArgumentException(
                    "Total number of seats exceeds aircraft model capacity"
            );
        }

        this.registration = registration;
        this.aircraftModel = aircraftModel;
        this.engineModels = new ArrayList<>(engineModels);
        this.cabinConfiguration = cabinConfiguration;
        this.registrationYear = registrationYear;
        this.country = country;
        this.status = status;
        this.company = company;
    }

    public AircraftRegistration registration() {
        return registration;
    }

    public AircraftModel aircraftModel() {
        return aircraftModel;
    }

    public AircraftStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == AircraftStatus.ACTIVE;
    }

    public CabinConfiguration cabinConfiguration() {
        return cabinConfiguration;
    }

    public RegistrationYear registrationYear() {
        return registrationYear;
    }

    public Country country() {
        return country;
    }

    public List<CurrentEngine> engineModels() {
        return Collections.unmodifiableList(engineModels);
    }

    public AirTransportCompany company() {
        return company;
    }

    public int age() {
        return registrationYear.age();
    }

    public void decommission() {
        if (this.status == AircraftStatus.DECOMMISSIONED) {
            throw new IllegalStateException("Aircraft is already decommissioned.");
        }

        this.status = AircraftStatus.DECOMMISSIONED;
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(final Object other) {

        if (!(other instanceof Aircraft that)) {
            return false;
        }

        return this.registration.equals(that.registration);
    }
}