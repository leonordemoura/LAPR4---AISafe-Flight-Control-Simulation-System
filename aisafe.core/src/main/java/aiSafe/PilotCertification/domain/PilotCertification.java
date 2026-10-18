package aiSafe.PilotCertification.domain;

import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.general.domain.model.EmailAddress;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "pilot_certifications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"pilot_id"})
)
public class PilotCertification implements AggregateRoot<PilotID> {

    @EmbeddedId
    private PilotID pilotId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "emailAddress",
                    column = @Column(name = "email")
            )
    })
    private Email userEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private AirTransportCompany company;

    @Column(nullable = false)
    private boolean active;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "pilot_model_certification",
            joinColumns = @JoinColumn(
                    name = "pilot_id",
                    referencedColumnName = "pilot_id"
            )
    )
    private Set<ModelCertification> certifications;

    protected PilotCertification() {
    }

    public PilotCertification(
            final PilotID pilotId,
            final Email userEmail,
            final AirTransportCompany company,
            final Set<ModelCertification> certifications
    ) {

        Preconditions.noneNull(
                pilotId,
                userEmail,
                company,
                certifications
        );

        if (certifications.isEmpty()) {
            throw new IllegalArgumentException(
                    "Pilot must have at least one certification"
            );
        }

        this.pilotId = pilotId;
        this.userEmail = userEmail;
        this.company = company;
        this.certifications = certifications;
        this.active = true;
    }

    public Email userEmail() {
        return userEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {

        this.active = false;
    }

    public void activate() {

        if (certifications.isEmpty()) {
            throw new IllegalStateException(
                    "Active pilot must have certifications"
            );
        }

        this.active = true;
    }

    public AirTransportCompany company() {
        return company;
    }

    public Set<ModelCertification> certifications() {
        return Set.copyOf(certifications);
    }

    @Override
    public PilotID identity() {
        return pilotId;
    }

    @Override
    public boolean sameAs(Object other) {

        if (!(other instanceof PilotCertification that)) {
            return false;
        }

        return this.pilotId.equals(that.pilotId);
    }
}