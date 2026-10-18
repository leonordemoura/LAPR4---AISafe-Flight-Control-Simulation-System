package aiSafe.airport.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;
import aiSafe.aircontrolarea.domain.AirControlArea;

import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "airport",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "iata_code"),
                @UniqueConstraint(columnNames = "icao_code"),
                @UniqueConstraint(columnNames = {
                        "latitude",
                        "longitude"
                })
        }
)
public class Airport implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "iataCode",
            column = @Column(name = "iata_code", nullable = false, unique = true)
    )
    private IataCode_Airport iataCode;

    @Embedded
    @AttributeOverride(
            name = "icaoCode",
            column = @Column(name = "icao_code", nullable = false, unique = true)
    )
    private IcaoCode_Airport icaoCode;

    @Embedded
    @AttributeOverride(
            name = "airportName",
            column = @Column(name = "airport_name", nullable = false)
    )
    private AirportName airportName;

    @Embedded
    @AttributeOverride(
            name = "town",
            column = @Column(name = "town", nullable = false)
    )
    private Town town;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "name",
                    column = @Column(name = "country_name", nullable = false)
            ),
            @AttributeOverride(
                    name = "code",
                    column = @Column(name = "country_code", nullable = false)
            )
    })
    private Country_Airport country;

    @Embedded
    @AttributeOverride(
            name = "elevation",
            column = @Column(name = "elevation", nullable = false)
    )
    private Elevation elevation;

    @AttributeOverrides({
            @AttributeOverride(
                    name = "latitude",
                    column = @Column(name = "latitude", nullable = false)
            ),
            @AttributeOverride(
                    name = "longitude",
                    column = @Column(name = "longitude", nullable = false)
            )
    })
    private Coordinate_Airport coordinate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "air_control_area_id")
    private AirControlArea airControlArea;

    protected Airport() {}

    public Airport(
            final IataCode_Airport iataCode,
            final IcaoCode_Airport icaoCode,
            final AirportName airportName,
            final Town town,
            final Country_Airport country,
            final Elevation elevation,
            final Coordinate_Airport coordinate,
            final AirControlArea airControlArea
    ) {

        Preconditions.ensure(iataCode != null, "iataCode must not be null");
        Preconditions.ensure(icaoCode != null, "icaoCode must not be null");
        Preconditions.ensure(airportName != null, "airportName must not be null");
        Preconditions.ensure(town != null, "town must not be null");
        Preconditions.ensure(country != null, "country must not be null");
        Preconditions.ensure(elevation != null, "elevation must not be null");
        Preconditions.ensure(coordinate != null, "coordinate must not be null");
        Preconditions.ensure(airControlArea != null, "airControlArea must not be null");

        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.airportName = airportName;
        this.town = town;
        this.country = country;
        this.elevation = elevation;
        this.coordinate = coordinate;
        this.airControlArea = airControlArea;
    }

    public IataCode_Airport iataCode() {
        return iataCode;
    }

    public IcaoCode_Airport icaoCode() {
        return icaoCode;
    }

    public AirportName airportName() {
        return airportName;
    }

    public Town town() {
        return town;
    }

    public Country_Airport country() {
        return country;
    }

    public Elevation elevation() {
        return elevation;
    }

    public Coordinate_Airport coordinate() {
        return coordinate;
    }

    public AirControlArea airControlArea() {
        return airControlArea;
    }

    @Override
    public Long identity() {
        return id;
    }

    @Override
    public boolean sameAs(Object other) {

        if (!(other instanceof Airport that)) {
            return false;
        }

        return iataCode.equals(that.iataCode)
                || icaoCode.equals(that.icaoCode);
    }
}