package aiSafe.flight.dsl.model;

import java.util.Objects;

public final class DslFuel {

    private final double amount;
    private final String unit;
    private final int line;

    public DslFuel(final double amount, final String unit, final int line) {
        this.amount = amount;
        this.unit = unit;
        this.line = line;
    }

    public double amount() {
        return amount;
    }

    public String unit() {
        return unit;
    }

    public int line() {
        return line;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DslFuel that)) return false;
        return line == that.line
                && Double.compare(that.amount, amount) == 0
                && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, unit, line);
    }
}
