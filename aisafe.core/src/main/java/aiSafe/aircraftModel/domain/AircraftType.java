package aiSafe.aircraftModel.domain;

public enum AircraftType {
    PASSENGER ("Passenger"),
    CARGO("Cargo"),
    MIXED ("Mixed");

    private final String description;

    AircraftType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}