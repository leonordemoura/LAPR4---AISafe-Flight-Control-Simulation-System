package aiSafe.weatherdata.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherEmbeddableAttributesTest {

    @Test
    void weather_enums_are_addressable_for_persistence_mappings() {
        assertEquals(2, PressureUnit.values().length);
        assertEquals(2, TemperatureUnit.values().length);
        assertEquals(3, WindSpeedUnit.values().length);
    }

    @Test
    void windDirection_rejects_out_of_range() {
        assertThrows(IllegalArgumentException.class, () -> new WindDirection(-0.001));
        assertThrows(IllegalArgumentException.class, () -> new WindDirection(360.0));
    }

    @Test
    void windDirection_equals_and_hash_code() {
        final WindDirection a = new WindDirection(90);
        assertEquals(a, new WindDirection(90));
        assertEquals(a.hashCode(), new WindDirection(90).hashCode());
        assertNotEquals(a, new WindDirection(91));
        assertFalse(a.equals("x"));
        assertFalse(a.equals(null));
    }

    @Test
    void windSpeed_rejects_negative_and_null_unit() {
        assertThrows(IllegalArgumentException.class, () -> new WindSpeed(-1, WindSpeedUnit.KMH));
        assertThrows(IllegalArgumentException.class, () -> new WindSpeed(5, null));
    }

    @Test
    void windSpeed_equals_and_hash_code() {
        final WindSpeed w = new WindSpeed(12, WindSpeedUnit.KT);
        assertEquals(w, new WindSpeed(12, WindSpeedUnit.KT));
        assertEquals(w.hashCode(), new WindSpeed(12, WindSpeedUnit.KT).hashCode());
        assertNotEquals(w, new WindSpeed(12.1, WindSpeedUnit.KT));
        assertFalse(w.equals(null));
    }

    @Test
    void temperature_rejects_null_unit() {
        assertThrows(IllegalArgumentException.class, () -> new Temperature(0, null));
    }

    @Test
    void temperature_equals_and_hash_code() {
        final Temperature t = new Temperature(-5, TemperatureUnit.C);
        assertEquals(t, new Temperature(-5, TemperatureUnit.C));
        assertEquals(t.hashCode(), new Temperature(-5, TemperatureUnit.C).hashCode());
        assertFalse(t.equals(new Temperature(-4, TemperatureUnit.C)));
        assertFalse(t.equals(null));
    }

    @Test
    void pressure_rejects_non_positive_value_and_null_unit() {
        assertThrows(IllegalArgumentException.class, () -> new Pressure(0, PressureUnit.HPA));
        assertThrows(IllegalArgumentException.class, () -> new Pressure(100, null));
    }

    @Test
    void orm_constructors_are_package_visible_for_mappings() {
        new WindDirection();
        new WindSpeed();
        new Temperature();
        new Pressure();
    }

    @Test
    void pressure_equals_and_hash_code() {
        final Pressure p = new Pressure(1000.1, PressureUnit.HPA);
        assertEquals(p, new Pressure(1000.1, PressureUnit.HPA));
        assertEquals(p.hashCode(), new Pressure(1000.1, PressureUnit.HPA).hashCode());
        assertFalse(p.equals(new Pressure(1001, PressureUnit.HPA)));
        assertFalse(p.equals(null));
    }
}
