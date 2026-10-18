package aiSafe.PilotCertification.domain;

import aiSafe.aircraftModel.domain.AircraftModelId;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.time.LocalDate;

@Embeddable
public class ModelCertification implements ValueObject {

    @Embedded
    private AircraftModelId aircraftModelId;

    private LocalDate certificationDate;

    protected ModelCertification() {
    }

    public ModelCertification(
            final AircraftModelId aircraftModelId,
            final LocalDate certificationDate
    ) {

        Preconditions.noneNull(aircraftModelId, certificationDate, "certificationDate and aircraftModelId cannot be null");

        if (certificationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Certification date cannot be in the future"
            );
        }

        this.aircraftModelId = aircraftModelId;
        this.certificationDate = certificationDate;
    }

    public AircraftModelId aircraftModelId() {
        return aircraftModelId;
    }

    public LocalDate certificationDate() {
        return certificationDate;
    }
}