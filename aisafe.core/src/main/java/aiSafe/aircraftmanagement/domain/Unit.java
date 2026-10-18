package aiSafe.aircraftmanagement.domain;

public enum Unit {
    HORSEPOWER("Horsepower"),
    KILOWATT("Kilowatt");

    private final String displayName;

    Unit(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
