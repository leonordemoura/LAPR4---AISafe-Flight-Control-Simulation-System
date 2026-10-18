package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

@Entity
@Table(
        name = "EngineModel",
        uniqueConstraints = @UniqueConstraint(columnNames = {"modelName", "maker_id"}))
public class EngineModel implements AggregateRoot<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long engineModelId;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "modelName"))
    private EngineModelName modelName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "PowerValue")),
        @AttributeOverride(name = "unit", column = @Column(name = "PowerUnit"))
    })
    private Power power;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "TSFCValue"))
    private TSFC tsfc;

    @ManyToOne(optional = false)
    @JoinColumn(name = "fuel_type_id", nullable = false)
    private FuelType fuelType;

    @Enumerated (EnumType.STRING)
    private MotorizationType engineType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "maker_id", referencedColumnName = "makerID", nullable = false)
    private Maker maker;

    protected EngineModel(){
        // for ORM
    }

    public EngineModel(final EngineModelName modelName, final Power power, final TSFC tsfc,
                       final FuelType fuelType, final MotorizationType engineType, final Maker maker) {
        Preconditions.noneNull(modelName, power, tsfc, fuelType, engineType, maker, "EngineModel attributes cannot be null.");
        Preconditions.nonNegative(power.value(),"Engine power must be non-negative.");
        Preconditions.ensure(tsfc.value() > 0, "Engine TSFC must be non-negative.");
        this.modelName = modelName;
        this.power = power;
        this.tsfc = tsfc;
        this.fuelType = fuelType;
        this.engineType = engineType;
        this.maker = maker;
    }

    public EngineModelName modelName(){
        return modelName;
    }

    public Long id() {
        return engineModelId;
    }

    public Maker maker(){
        return maker;
    }

    public MotorizationType engineType(){
        return engineType;
    }

    public Power power() {
        return power;
    }

    public TSFC tsfc() {
        return tsfc;
    }

    public FuelType fuelType() {
        return fuelType;
    }

    @Override
    public Long identity() {
        return engineModelId;
    }

    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (!(other instanceof EngineModel)) return false;
        EngineModel o = (EngineModel) other;
        return modelName.equals(o.modelName) && maker.equals(o.maker);
    }
}
