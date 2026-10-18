package aiSafe.aircraftModel.domain;

public enum SpeedUnit {
    KMH("km/h"),
    KNOTS("knots");

    private final String label;

    SpeedUnit(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
