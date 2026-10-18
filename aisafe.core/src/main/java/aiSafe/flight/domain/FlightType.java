package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;

public enum FlightType implements ValueObject, Serializable {
    REGULAR,
    CHARTER
}
