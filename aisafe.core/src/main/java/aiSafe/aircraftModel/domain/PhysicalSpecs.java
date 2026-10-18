package aiSafe.aircraftModel.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

@Embeddable
public class PhysicalSpecs {

    @Embedded
    private Altitude serviceCeiling;

    @Embedded
    private Speed cruiseSpeed;

    @Embedded
    private Area wingArea;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "empty_weight_value")),
            @AttributeOverride(name = "unit", column = @Column(name = "empty_weight_unit"))
    })
    private Weight emptyWeight;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "mtow_value")),
            @AttributeOverride(name = "unit", column = @Column(name = "mtow_unit"))
    })
    private Weight mtow;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "mzfw_value")),
            @AttributeOverride(name = "unit", column = @Column(name = "mzfw_unit"))
    })
    private Weight mzfw;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "fuelCapacity"))
    private Weight fuelCapacity;

    @Embedded
    private LiftCoefficient liftCoefficient;

    @Embedded
    private DragCoefficient dragCoefficient;

    protected PhysicalSpecs() {}

    public PhysicalSpecs(
            Altitude serviceCeiling,
            Speed cruiseSpeed,
            Area wingArea,
            Weight emptyWeight,
            Weight mtow,
            Weight mzfw,
            Weight fuelCapacity,
            LiftCoefficient liftCoefficient,
            DragCoefficient dragCoefficient
    ) {

        Preconditions.noneNull(
                serviceCeiling,
                cruiseSpeed,
                wingArea,
                emptyWeight,
                mtow,
                mzfw,
                fuelCapacity,
                liftCoefficient,
                dragCoefficient
        );

        Preconditions.ensure(serviceCeiling.value() > 0, "Invalid altitude");
        Preconditions.ensure(cruiseSpeed.value() > 0, "Invalid speed");
        Preconditions.ensure(wingArea.value() > 0, "Invalid wing area");

        Preconditions.ensure(
                emptyWeight.value() < mtow.value(),
                "Empty weight must be less than MTOW"
        );

        Preconditions.ensure(
                mzfw.value() <= mtow.value(),
                "MZFW must be <= MTOW"
        );

        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.wingArea = wingArea;
        this.emptyWeight = emptyWeight;
        this.mtow = mtow;
        this.mzfw = mzfw;
        this.fuelCapacity = fuelCapacity;
        this.liftCoefficient = liftCoefficient;
        this.dragCoefficient = dragCoefficient;
    }

    public Altitude serviceCeiling() {
        return serviceCeiling;
    }

    public Speed cruiseSpeed() {
        return cruiseSpeed;
    }

    public Area wingArea() {
        return wingArea;
    }

    public Weight emptyWeight() {
        return emptyWeight;
    }

    public Weight mtow() {
        return mtow;
    }

    public Weight mzfw() {
        return mzfw;
    }

    public Weight fuelCapacity() {
        return fuelCapacity;
    }

    public LiftCoefficient liftCoefficient() {
        return liftCoefficient;
    }

    public DragCoefficient dragCoefficient() {
        return dragCoefficient;
    }
}