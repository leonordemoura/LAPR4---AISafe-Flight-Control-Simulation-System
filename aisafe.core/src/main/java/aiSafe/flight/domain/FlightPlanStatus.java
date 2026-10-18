package aiSafe.flight.domain;

import eapli.framework.domain.model.ValueObject;

import java.io.Serializable;

public enum FlightPlanStatus implements ValueObject, Serializable {
    DRAFT,
    VALIDATED,
    CANCELLED
}
