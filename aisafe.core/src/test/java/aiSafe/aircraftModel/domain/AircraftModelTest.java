package aiSafe.aircraftModel.domain;

import aiSafe.aircraftmanagement.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    // =========================================================
    // AUX METHODS (builders)
    // =========================================================

    private Maker validMaker() {
        return new Maker(
                new MakerID(123L),
                new MakerName("Airbus"),
                new Country("France", 351)
        );
    }

    private FuelType validFuelType() {
        return new FuelType(
                new FuelTypeName("JET A"),
                new FuelTypeDensity(0.8),
                new FuelTypeSpecificEnergy(43.0)
        );
    }

    private EngineModel validEngine(MotorizationType type) {
        return new EngineModel(
                new EngineModelName("CFM56"),
                new Power(27000, Unit.HORSEPOWER),
                new TSFC(0.5),
                validFuelType(),
                type,
                validMaker()
        );
    }

    private PhysicalSpecs validSpecs() {
        return new PhysicalSpecs(
                new Altitude(12000, AltitudeUnit.METERS),
                new Speed(850, SpeedUnit.KMH),
                new Area(122.6, AreaUnit.m2),
                new Weight(41000, WeightUnit.KG),
                new Weight(75000, WeightUnit.KG),
                new Weight(60000, WeightUnit.KG),
                new Weight(20000, WeightUnit.KG),
                new LiftCoefficient(0.5),
                new DragCoefficient(0.02)
        );
    }

    private AircraftModel buildValidModel() {
        return new AircraftModel(
                new AircraftModelId("MODEL-001"),
                new AircraftModelName("A320"),
                validMaker(),
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(180)
        );
    }

    // =========================================================
    // TEST 1 - SUCCESS
    // =========================================================

    @Test
    void ensureAircraftModelIsCreatedSuccessfullyWithValidData() {

        final var model = buildValidModel();

        assertNotNull(model);
    }

    // =========================================================
    // TEST 2 - NO ENGINES
    // =========================================================

    @Test
    void ensureAircraftModelMustHaveAtLeastOneEngine() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(
                    new AircraftModelId("MODEL-002"),
                    new AircraftModelName("A320-TEST"),
                    validMaker(),
                    List.of(), // vazio
                    validSpecs(),
                    AircraftType.PASSENGER,
                    new MaximumSeats(180)
            );
        });
    }

    // =========================================================
    // TEST 3 - DIFFERENT ENGINE TYPES
    // =========================================================

    @Test
    void ensureAllEnginesMustBeOfSameType() {

        final var engine1 = validEngine(MotorizationType.TURBOFAN);
        final var engine2 = validEngine(MotorizationType.TURBOPROP);

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(
                    new AircraftModelId("MODEL-003"),
                    new AircraftModelName("B737"),
                    validMaker(),
                    List.of(engine1, engine2),
                    validSpecs(),
                    AircraftType.PASSENGER,
                    new MaximumSeats(200)
            );
        });
    }

    // =========================================================
    // TEST 4 - INVALID NAME
    // =========================================================

    @Test
    void ensureAircraftModelNameCannotBeEmpty() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(
                    new AircraftModelId("MODEL-004"),
                    new AircraftModelName(""),
                    validMaker(),
                    List.of(validEngine(MotorizationType.TURBOFAN)),
                    validSpecs(),
                    AircraftType.PASSENGER,
                    new MaximumSeats(150)
            );
        });
    }

    // =========================================================
    // TEST 5 - INVALID PHYSICAL SPECS
    // =========================================================

    @Test
    void ensurePhysicalParametersMustBePositive() {

        assertThrows(IllegalArgumentException.class, () -> {

            final var invalidSpecs = new PhysicalSpecs(
                    new Altitude(-100, AltitudeUnit.METERS), // inválido
                    new Speed(0, SpeedUnit.KMH),
                    new Area(122.6, AreaUnit.m2),
                    new Weight(-1, WeightUnit.KG),
                    new Weight(75000, WeightUnit.KG),
                    new Weight(60000, WeightUnit.KG),
                    new Weight(20000, WeightUnit.KG),
                    new LiftCoefficient(0.5),
                    new DragCoefficient(0.02)
            );

            new AircraftModel(
                    new AircraftModelId("MODEL-005"),
                    new AircraftModelName("INVALID"),
                    validMaker(),
                    List.of(validEngine(MotorizationType.TURBOFAN)),
                    invalidSpecs,
                    AircraftType.PASSENGER,
                    new MaximumSeats(100)
            );
        });
    }

    // =========================================================
    // TEST 6 - NULL FIELDS
    // =========================================================

    @Test
    void ensureMandatoryFieldsCannotBeNull() {

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(
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
    // TEST 7 - sameAs TRUE (same name + maker)
    // =========================================================

    @Test
    void ensureSameAsReturnsTrueForSameNameAndMaker() {

        final var maker = validMaker();

        final var model1 = new AircraftModel(
                new AircraftModelId("ID-1"),
                new AircraftModelName("A320"),
                maker,
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(180)
        );

        final var model2 = new AircraftModel(
                new AircraftModelId("ID-2"),
                new AircraftModelName("A320"), // igual
                maker,
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(200)
        );

        assertTrue(model1.sameAs(model2));
    }

    // =========================================================
    // TEST 8 - sameAs FALSE
    // =========================================================

    @Test
    void ensureSameAsReturnsFalseForDifferentModels() {

        final var model1 = buildValidModel();

        final var model2 = new AircraftModel(
                new AircraftModelId("MODEL-999"),
                new AircraftModelName("B737"),
                validMaker(),
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.CARGO,
                new MaximumSeats(120)
        );

        assertFalse(model1.sameAs(model2));
    }

    // =========================================================
    // TEST 9 - IDENTITY
    // =========================================================

    @Test
    void ensureIdentityIsCorrect() {

        final var model = buildValidModel();

        assertEquals("MODEL-001", model.identity().toString());
    }

    // =========================================================
    // TEST 10 - GETTERS
    // =========================================================

    @Test
    void ensureGettersReturnCorrectValues() {

        final var model = buildValidModel();

        assertEquals("A320", model.name().toString());
        assertNotNull(model.maker());
    }

    @Test
    void ensureMaximumSeatsGetterReturnsCorrectValue() {

        final var model = buildValidModel();

        MaximumSeats maxSeats = new MaximumSeats(180);
        assertEquals(maxSeats, model.maximumSeats());
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentObjectType() {

        final var model = buildValidModel();

        assertFalse(model.sameAs("not an aircraft"));
    }

    @Test
    void ensureAircraftModelAcceptsMultipleEnginesOfSameType() {

        final var engine1 = validEngine(MotorizationType.TURBOFAN);
        final var engine2 = validEngine(MotorizationType.TURBOFAN);

        final var model = new AircraftModel(
                new AircraftModelId("MODEL-MULTI"),
                new AircraftModelName("A330"),
                validMaker(),
                List.of(engine1, engine2),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(300)
        );

        assertNotNull(model);
    }

    @Test
    void ensureIdentityReturnsCorrectObject() {

        final var id = new AircraftModelId("MODEL-777");

        final var model = new AircraftModel(
                id,
                new AircraftModelName("B777"),
                validMaker(),
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(350)
        );

        assertEquals(id, model.identity());
    }

    @Test
    void ensureNameGetterReturnsCorrectObject() {

        final var name = new AircraftModelName("A350");

        final var model = new AircraftModel(
                new AircraftModelId("MODEL-A350"),
                name,
                validMaker(),
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(320)
        );

        assertEquals(name, model.name());
    }

    @Test
    void ensureMakerGetterReturnsCorrectMaker() {

        final var maker = validMaker();

        final var model = new AircraftModel(
                new AircraftModelId("MODEL-MAKER"),
                new AircraftModelName("A321"),
                maker,
                List.of(validEngine(MotorizationType.TURBOFAN)),
                validSpecs(),
                AircraftType.PASSENGER,
                new MaximumSeats(220)
        );

        assertEquals(maker, model.maker());
    }
}