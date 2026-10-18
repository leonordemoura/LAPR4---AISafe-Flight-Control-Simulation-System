package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaNameTest {

     @Test
     void ensureAreaNameCannotBeNull() {
         // Arrange
         String nullName = null;

         // Act & Assert
         assertThrows(IllegalArgumentException.class, () -> {
             new AreaName(nullName);
         });
     }

     @Test
     void ensureAreaNameCannotBeEmpty() {
         // Arrange
         String emptyName = "";

         // Act & Assert
         assertThrows(IllegalArgumentException.class, () -> {
             new AreaName(emptyName);
         });
     }

     @Test
     void ensureAreaNameCannotBeBlank() {
         // Arrange
         String blankName = "   ";

         // Act & Assert
         assertThrows(IllegalArgumentException.class, () -> {
             new AreaName(blankName);
         });
     }

     @Test
     void ensureAreaNameIsCreatedSuccessfully() {
         // Arrange
         String value = "Lisbon Control Area";

         // Act
         AreaName areaName = new AreaName(value);

         // Assert
         assertEquals(value, areaName.value());
     }

     @Test
     void ensureAreaNameTrimsWhitespace() {
         assertEquals("North", new AreaName("  North  ").value());
     }

     @Test
     void ensureEqualsHashCodeAndToString() {
         final AreaName a = new AreaName("Alpha");
         final AreaName b = new AreaName("Alpha");
         assertEquals(a, b);
         assertEquals(a.hashCode(), b.hashCode());
         assertEquals("Alpha", a.toString());
         assertNotEquals(a, new AreaName("Beta"));
         assertFalse(a.equals(null));
         assertFalse(a.equals("Alpha"));
     }

}