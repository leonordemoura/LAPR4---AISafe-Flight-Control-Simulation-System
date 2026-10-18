package aiSafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class MakerTest {

    @Test
    void ensureMakerIsCreatedSuccessfully() {
        // Arrange
        final Maker maker = new Maker(
                MakerID.valueOf(124L),
                MakerName.valueOf("GE Aviation"),
                Country.valueOf("USA", 840)
        );

        // Act & Assert
        assertNotNull(maker, "Maker should be created successfully");
        assertEquals(124L, maker.identity().id(), "Maker ID should be set correctly");
        assertEquals(MakerID.valueOf(124L), maker.makerID(), "makerID() should expose the embedded identity");
        assertEquals("GE Aviation", maker.name().name(), "Maker name should be set correctly");
        assertEquals("USA", maker.country().name(), "Maker country name should be set correctly");
    }

    @Test
    void ensureTwoMakersWithSameIdAreDuplicates() {
        // Arrange
        final MakerID id = MakerID.valueOf(124L);
        final Maker maker1 = new Maker(id, MakerName.valueOf("GE Aviation"), Country.valueOf("USA", 840));
        final Maker maker2 = new Maker(id, MakerName.valueOf("Different Name"), Country.valueOf("USA", 840));

        // Act & Assert
        assertTrue(maker1.sameAs(maker2), "Makers with the same ID should be considered duplicates");
        assertEquals(maker1.hashCode(), maker2.hashCode(), "Makers with the same ID should have the same hash code");
    }

    @Test
    void ensureMakersWithDifferentIdAreNotSameAggregate() {
        // Arrange
        final Maker maker1 = new Maker(MakerID.valueOf(124L), MakerName.valueOf("GE Aviation"), Country.valueOf("USA", 840));
        final Maker maker2 = new Maker(MakerID.valueOf(125L), MakerName.valueOf("GE Aviation"), Country.valueOf("USA", 840));

        // Act & Assert
        assertFalse(maker1.sameAs(maker2), "Makers with different IDs should not be considered the same aggregate");
    }

    @Test
    void ensureMakerCannotBeCreatedWithNullAttributes() {
        // Arrange
        final MakerID id = MakerID.valueOf(124L);
        final MakerName name = MakerName.valueOf("GE Aviation");
        final Country country = Country.valueOf("USA", 840);

        // Act & Assert
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new Maker(null, name, country), "Creating a Maker with null ID should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> new Maker(id, null, country), "Creating a Maker with null name should throw an exception"),
                () -> assertThrows(IllegalArgumentException.class, () -> new Maker(id, name, null), "Creating a Maker with null country should throw an exception")
        );
    }

    @Test
    void sameAs_is_true_for_same_instance() {
        // Arrange
        final Maker m = new Maker(MakerID.valueOf(1L), MakerName.valueOf("Acme"), Country.valueOf("PT", 620));

        // Act & Assert
        assertTrue(m.sameAs(m));
    }

    @Test
    void sameAs_is_false_for_null_or_wrong_type() {
        // Arrange
        final Maker m = new Maker(MakerID.valueOf(1L), MakerName.valueOf("Acme"), Country.valueOf("PT", 620));

        // Act & Assert
        assertFalse(m.sameAs(null));
        assertFalse(m.sameAs("x"));
    }

    @Test
    void toString_returns_maker_name_text() {
        // Arrange
        final Maker m = new Maker(MakerID.valueOf(1L), MakerName.valueOf("Acme SA"), Country.valueOf("PT", 620));

        // Act & Assert
        assertEquals("Acme SA", m.toString());
    }

    @Test
    void protected_no_arg_constructor_is_exposed_for_jpa() throws Exception {
        // Arrange
        final Constructor<Maker> ctor = Maker.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        // Act
        final Maker empty = ctor.newInstance();

        // Assert — entity shell used by the persistence provider; fields remain unset
        assertNotNull(empty);
    }
}