package aiSafe.aircraftModel.domain;

public enum WeightUnit {
    KG("kg");

    private final String label;

    WeightUnit(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
