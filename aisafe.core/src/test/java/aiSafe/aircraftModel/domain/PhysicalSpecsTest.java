package aiSafe.aircraftModel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalSpecsTest {

    private PhysicalSpecs buildValidPhysicalSpecs() {

        return new PhysicalSpecs(
                new Altitude(12000, AltitudeUnit.METERS),
                new Speed(850, SpeedUnit.KMH),
                new Area(122.6, AreaUnit.m2),
                new Weight(41000, WeightUnit.KG),
                new Weight(75000, WeightUnit.KG),
                new Weight(60000, WeightUnit.KG),
                new Weight(20000, WeightUnit.KG),
                new LiftCoefficient(1.5),
                new DragCoefficient(0.03)
        );
    }

    // =========================================================
    // TEST 1
    // =========================================================

    @Test
    void ensurePhysicalSpecsIsCreatedSuccessfullyWithValidData() {

        final var specs = buildValidPhysicalSpecs();

        assertNotNull(specs);
    }

    // =========================================================
    // TEST 2
    // =========================================================

    @Test
    void ensureNullFieldsAreRejected() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        });
    }

    // =========================================================
    // TEST 3
    // =========================================================

    @Test
    void ensureInvalidAltitudeIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    new Altitude(0, AltitudeUnit.METERS),
                    new Speed(850, SpeedUnit.KMH),
                    new Area(122.6, AreaUnit.m2),
                    new Weight(41000, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(60000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(1.5),
                    new DragCoefficient(0.03)
            );
        });
    }

    // =========================================================
    // TEST 4
    // =========================================================

    @Test
    void ensureInvalidSpeedIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    new Altitude(12000, AltitudeUnit.METERS),
                    new Speed(0, SpeedUnit.KMH),
                    new Area(122.6, AreaUnit.m2),
                    new Weight(41000, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(60000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(1.5),
                    new DragCoefficient(0.03)
            );
        });
    }

    // =========================================================
    // TEST 5
    // =========================================================

    @Test
    void ensureInvalidWingAreaIsRejected() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    new Altitude(12000, AltitudeUnit.METERS),
                    new Speed(850, SpeedUnit.KMH),
                    new Area(0, AreaUnit.m2),
                    new Weight(41000, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(60000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(1.5),
                    new DragCoefficient(0.03)
            );
        });
    }

    // =========================================================
    // TEST 6
    // =========================================================

    @Test
    void ensureEmptyWeightMustBeLessThanMtow() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    new Altitude(12000, AltitudeUnit.METERS),
                    new Speed(850, SpeedUnit.KMH),
                    new Area(122.6, AreaUnit.m2),
                    new Weight(80000, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(60000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(1.5),
                    new DragCoefficient(0.03)
            );
        });
    }

    // =========================================================
    // TEST 7
    // =========================================================

    @Test
    void ensureMzfwMustBeLessOrEqualThanMtow() {

        assertThrows(IllegalArgumentException.class, () -> {

            new PhysicalSpecs(
                    new Altitude(12000, AltitudeUnit.METERS),
                    new Speed(850, SpeedUnit.KMH),
                    new Area(122.6, AreaUnit.m2),
                    new Weight(41000, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(80000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(1.5),
                    new DragCoefficient(0.03)
            );
        });
    }

    // =========================================================
    // TEST 8
    // =========================================================

    @Test
    void ensureDifferentPhysicalSpecsObjectsAreCreatedIndependently() {

        final var specs1 = buildValidPhysicalSpecs();
        final var specs2 = buildValidPhysicalSpecs();

        assertNotSame(specs1, specs2);
    }
}