package aiSafe.flight.application.validate.simulator;

import aiSafe.flight.domain.FuelQuantity;

final class FuelQuantityKg {

    private FuelQuantityKg() {
    }

    static double toKg(final FuelQuantity fuel, final double densityKgPerL) {
        final String unit = fuel.unit().trim().toLowerCase();
        if (unit.equals("kg") || unit.equals("kilogram") || unit.equals("kilograms")) {
            return fuel.value();
        }
        if (unit.equals("l")
                || unit.equals("liter")
                || unit.equals("liters")
                || unit.equals("litre")
                || unit.equals("litres")) {
            return fuel.value() * densityKgPerL;
        }
        throw new IllegalArgumentException("Unsupported fuel unit for simulation: " + fuel.unit());
    }
}
