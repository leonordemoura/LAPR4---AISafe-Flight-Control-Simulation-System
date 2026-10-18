package aiSafe.aircraftModel.domain;

public enum AreaUnit {
    m2("m2");

    private final String label;

    AreaUnit(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}

