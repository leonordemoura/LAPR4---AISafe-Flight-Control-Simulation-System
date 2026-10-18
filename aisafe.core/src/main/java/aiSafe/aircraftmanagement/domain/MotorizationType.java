package aiSafe.aircraftmanagement.domain;

public enum MotorizationType {
    TURBOPROP ("Turboprop"),
    TURBOFAN ("Turbofan"),
    TURBOJET ("Turbojet"),
    RAMJET ("Ramjet"),
    ELECTRIC_PROPELLER ("Electric Propeller");

    private final String description;

    MotorizationType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
