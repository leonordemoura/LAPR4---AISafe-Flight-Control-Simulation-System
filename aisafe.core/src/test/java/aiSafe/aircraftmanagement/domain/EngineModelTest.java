package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelTest {
    private Maker maker;
    private FuelType fuelType;

    @BeforeEach
    void setup() {
        maker = new Maker(
                MakerID.valueOf(124L),
                MakerName.valueOf("GE Aviation"),
                Country.valueOf("USA", 840)
        );

        fuelType = new FuelType(
                FuelTypeName.valueOf("JET A"),
                FuelTypeDensity.valueOf(0.84),
                FuelTypeSpecificEnergy.valueOf(42.8)
        );
    }

    @Test
    void ensureEngineModelIsCreatedSuccessfully() {
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);
        MotorizationType motorizationType = MotorizationType.TURBOFAN;

        final var model = new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker);

        // Act & Assert
        assertNotNull(model, "EngineModel should be created successfully with valid attributes");
    }

    @Test
    void ensureTwoEngineModelsWithSameNameAndMakerAreEqual() {
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);
        MotorizationType motorizationType = MotorizationType.TURBOFAN;

        final var model1 = new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker);
        final var model2 = new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker);

        // Act & Assert
        assertTrue(model1.sameAs(model2), "Two EngineModels with the same name and maker should be considered the same");
    }

    @Test
    void ensureEngineModelNameCannotBeNull(){
        // Arrange
        EngineModelName modelName = null;
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);
        MotorizationType motorizationType = MotorizationType.TURBOFAN;


        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker));
    }

    @Test
    void  ensureEngineModelTSFCCannotBeNegative(){
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        Double tsfc = -0.5;
        MotorizationType motorizationType = MotorizationType.TURBOFAN;

        // Acts & Assert
        assertThrows(IllegalArgumentException.class, () -> new EngineModel(modelName, power, TSFC.valueOf(tsfc), fuelType, motorizationType, maker));
    }

    @Test
    void  ensureEngineModelPowerCannotBeNegative(){
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Integer negativePowerValue = -27000;
        Unit powerUnit = Unit.HORSEPOWER;
        TSFC tsfc = TSFC.valueOf(0.5);
        MotorizationType motorizationType = MotorizationType.TURBOFAN;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new EngineModel(modelName, Power.valueOf(negativePowerValue, powerUnit), tsfc, fuelType, motorizationType, maker));
    }

    @Test
    void ensureInvalidEngineTypeCannotBeNull(){
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);
        MotorizationType motorizationType = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker));
    }

    @Test
    void ensureFuelTypeCannotBeNull(){
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);
        FuelType fuelType = null;
        MotorizationType motorizationType = MotorizationType.TURBOFAN;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new EngineModel(modelName, power, tsfc, fuelType, motorizationType, maker));
    }

    @Test
    void ensureMakerCannotBeNull() {
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);
        TSFC tsfc = TSFC.valueOf(0.5);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new EngineModel(modelName, power, tsfc, fuelType, MotorizationType.TURBOFAN, null));
    }

    @Test
    void ensurePowerCannotBeNull() {
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        TSFC tsfc = TSFC.valueOf(0.5);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new EngineModel(modelName, null, tsfc, fuelType, MotorizationType.TURBOFAN, maker));
    }

    @Test
    void ensureTsfcCannotBeNull() {
        // Arrange
        EngineModelName modelName = EngineModelName.valueOf("GE90B");
        Power power = Power.valueOf(27000, Unit.HORSEPOWER);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new EngineModel(modelName, power, null, fuelType, MotorizationType.TURBOFAN, maker));
    }

    @Test
    void sameAs_is_true_for_same_instance() {
        // Arrange
        final EngineModel model = new EngineModel(
                EngineModelName.valueOf("GE90B"),
                Power.valueOf(27000, Unit.HORSEPOWER),
                TSFC.valueOf(0.5),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);

        // Act & Assert
        assertTrue(model.sameAs(model));
    }

    @Test
    void sameAs_is_false_for_null() {
        // Arrange
        final EngineModel model = new EngineModel(
                EngineModelName.valueOf("GE90B"),
                Power.valueOf(27000, Unit.HORSEPOWER),
                TSFC.valueOf(0.5),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);

        // Act & Assert
        assertFalse(model.sameAs(null));
    }

    @Test
    void sameAs_is_false_for_non_engine_model() {
        // Arrange
        final EngineModel model = new EngineModel(
                EngineModelName.valueOf("GE90B"),
                Power.valueOf(27000, Unit.HORSEPOWER),
                TSFC.valueOf(0.5),
                fuelType,
                MotorizationType.TURBOFAN,
                maker);

        // Act & Assert
        assertFalse(model.sameAs("not an engine"));
    }

    @Test
    void sameAs_is_false_when_same_name_but_different_maker() {
        // Arrange
        final Maker otherMaker = new Maker(
                MakerID.valueOf(200L),
                MakerName.valueOf("Other"),
                Country.valueOf("UK", 826));
        final EngineModelName sharedName = EngineModelName.valueOf("GE90B");
        final EngineModel a = new EngineModel(
                sharedName, Power.valueOf(1, Unit.KILOWATT), TSFC.valueOf(0.5),
                fuelType, MotorizationType.TURBOFAN, maker);
        final EngineModel b = new EngineModel(
                sharedName, Power.valueOf(1, Unit.KILOWATT), TSFC.valueOf(0.5),
                fuelType, MotorizationType.TURBOFAN, otherMaker);

        // Act & Assert
        assertFalse(a.sameAs(b));
    }

    @Test
    void sameAs_is_false_when_same_maker_but_different_model_name() {
        // Arrange
        final EngineModel a = new EngineModel(
                EngineModelName.valueOf("A"),
                Power.valueOf(1, Unit.KILOWATT), TSFC.valueOf(0.5),
                fuelType, MotorizationType.TURBOFAN, maker);
        final EngineModel b = new EngineModel(
                EngineModelName.valueOf("B"),
                Power.valueOf(1, Unit.KILOWATT), TSFC.valueOf(0.5),
                fuelType, MotorizationType.TURBOFAN, maker);

        // Act & Assert
        assertFalse(a.sameAs(b));
    }

    @Test
    void accessors_return_embedded_values() {
        // Arrange
        final EngineModelName name = EngineModelName.valueOf("ACCESS");
        final MotorizationType type = MotorizationType.TURBOPROP;
        final EngineModel model = new EngineModel(
                name,
                Power.valueOf(100, Unit.HORSEPOWER),
                TSFC.valueOf(0.2),
                fuelType,
                type,
                maker);

        // Act & Assert
        assertEquals(name, model.modelName());
        assertEquals(maker, model.maker());
        assertEquals(type, model.engineType());
        assertNull(model.identity());
        assertNull(model.identity());
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        // Arrange
        final Constructor<EngineModel> ctor = EngineModel.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        // Act
        final EngineModel empty = ctor.newInstance();

        // Assert — entity shell used by the persistence provider; fields remain unset
        assertNotNull(empty);
    }
}