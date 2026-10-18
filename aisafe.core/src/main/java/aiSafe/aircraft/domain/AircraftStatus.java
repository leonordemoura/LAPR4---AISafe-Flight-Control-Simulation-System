package aiSafe.aircraft.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;

public enum AircraftStatus implements ValueObject, Serializable {
    ACTIVE,
    DECOMMISSIONED
}