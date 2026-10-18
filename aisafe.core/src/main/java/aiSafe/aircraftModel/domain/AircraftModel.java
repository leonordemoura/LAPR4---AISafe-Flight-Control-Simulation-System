package aiSafe.aircraftModel.domain;

import aiSafe.aircraftmanagement.domain.Maker;
import aiSafe.aircraftmanagement.domain.EngineModel;
import aiSafe.aircraftmanagement.domain.MotorizationType;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(
        name = "aircraftModel",
        uniqueConstraints = @UniqueConstraint(columnNames = {"aircraft_model_name", "maker_id"})
)
public class AircraftModel implements AggregateRoot<AircraftModelId> {

    @EmbeddedId
    private AircraftModelId id;

    @Embedded
    @AttributeOverride(
            name = "aircraftModelName",
            column = @Column(name = "aircraft_model_name")
    )
    private AircraftModelName name;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "maker_id",
            referencedColumnName = "makerID",
            nullable = false
    )
    private Maker maker;

    @Enumerated(EnumType.STRING)
    private AircraftType type;


    @Embedded
    private MaximumSeats maximumSeats;

    public MaximumSeats maximumSeats() {
        return maximumSeats;
    }

    @ManyToMany
    @JoinTable(
            name = "aircraft_model_engine",
            joinColumns = @JoinColumn(
                    name = "aircraft_model_id",
                    referencedColumnName = "aircraft_model_id"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "engine_model_id",
                    referencedColumnName = "engineModelId"
            )
    )
    private List<EngineModel> engines;

    @Embedded
    private PhysicalSpecs specs;

    protected AircraftModel() {}

    public AircraftModel(
            AircraftModelId id,
            AircraftModelName name,
            Maker maker,
            List<EngineModel> engines,
            PhysicalSpecs specs,
            AircraftType type,
            MaximumSeats maximumSeats
    ) {
        Preconditions.noneNull(id, name, maker, engines, specs, type, maximumSeats);

        if (engines.isEmpty()) {
            throw new IllegalArgumentException("At least one engine required");
        }

        validateEngines(engines);

        this.id = id;
        this.name = name;
        this.maker = maker;
        this.engines = List.copyOf(engines);
        this.specs = specs;
        this.type = type;
        this.maximumSeats = maximumSeats;
    }

    private void validateEngines(List<EngineModel> engines) {

        final var firstType = engines.get(0).engineType();

        boolean sameType = engines.stream()
                .allMatch(e -> e.engineType() == firstType);

        if (!sameType) {
            throw new IllegalArgumentException(
                    "All engines must have the same motorization type"
            );
        }
    }

    @Override
    public AircraftModelId identity() {
        return id;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AircraftModel that)) return false;
        return name.equals(that.name) && maker.equals(that.maker);
    }

    public AircraftModelName name() { return name; }

    public Maker maker() { return maker; }

    public PhysicalSpecs specs() {
        return specs;
    }

    public List<EngineModel> engines() {
        return List.copyOf(engines);
    }

    public AircraftType type() {
        return type;
    }
}