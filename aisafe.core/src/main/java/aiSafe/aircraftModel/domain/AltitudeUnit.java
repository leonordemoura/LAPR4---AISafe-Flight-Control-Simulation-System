package aiSafe.aircraftModel.domain;

public enum AltitudeUnit {
    FEET("ft"),
    METERS("m");

    private final String label;

    AltitudeUnit(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
