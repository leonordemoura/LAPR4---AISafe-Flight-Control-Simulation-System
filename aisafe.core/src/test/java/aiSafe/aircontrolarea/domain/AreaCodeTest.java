package aiSafe.aircontrolarea.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AreaCodeTest {

    @Test
    void ensureAreaCodeCannotBeNull() {
        // Arrange
        String nullCode = null;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AreaCode(nullCode);
        });
    }

    @Test
    void ensureAreaCodeCannotBeEmpty() {
        // Arrange
        String emptyCode = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AreaCode(emptyCode);
        });
    }

    @Test
    void ensureAreaCodeCannotBeBlank() {
        // Arrange
        String blankCode = "   ";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new AreaCode(blankCode);
        });
    }

    @Test
    public void ensureAreaCodeIsCreatedSuccessfully() {
        // Arrange
        String value = "LPPT";

        // Act
        AreaCode areaCode = new AreaCode(value);

        // Assert
        assertEquals(value, areaCode.value());
    }

    @Test
    void ensureAreaCodeTrimsWhitespace() {
        assertEquals("LX", new AreaCode("  LX  ").value());
    }

    @Test
    void ensureEqualsHashCodeAndToString() {
        final AreaCode a = new AreaCode("AB");
        final AreaCode b = new AreaCode("AB");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals("AB", a.toString());
        assertNotEquals(a, new AreaCode("CD"));
        assertFalse(a.equals(null));
        assertFalse(a.equals("AB"));
    }

}