package aiSafe.aircontrolarea.domain;

import aiSafe.costumermanagement.domain.Costumer;
import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.usermanagement.domain.Email;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "air_control_area")
public class AirControlArea implements AggregateRoot<Long>, Costumer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private AreaCode areaCode;

    @Embedded
    private AreaName areaName;

    @Embedded
    private MinFuelAtArrival minFuelAtArrival;

    @Embedded
    private MaxAltitude maxAltitude;

    @Embedded
    private GeoBoundary geoBoundary;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "air_control_area_boundary_points",
            joinColumns = @JoinColumn(name = "air_control_area_id", referencedColumnName = "id")
    )
    @OrderColumn(name = "point_order")
    private List<Coordinate> geoBoundaryCoordinates;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "air_control_area_memberships",
            joinColumns = @JoinColumn(name = "air_control_area_id", referencedColumnName = "id")
    )
    private List<Membership> memberships;

    protected AirControlArea() {
        // For ORM.
    }

    public AirControlArea(
            final AreaCode areaCode,
            final AreaName areaName,
            final MinFuelAtArrival minFuelAtArrival,
            final MaxAltitude maxAltitude,
            final GeoBoundary geoBoundary
    ) {
        if (areaCode == null || areaName == null || minFuelAtArrival == null || maxAltitude == null || geoBoundary == null) {
            throw new IllegalArgumentException("AirControlArea mandatory fields must not be null");
        }
        this.areaCode = areaCode;
        this.areaName = areaName;
        this.minFuelAtArrival = minFuelAtArrival;
        this.maxAltitude = maxAltitude;
        this.geoBoundary = geoBoundary;
        this.geoBoundaryCoordinates = List.copyOf(geoBoundary.coordinates());
        this.memberships = new ArrayList<>();
    }

    public List<Membership> memberships() {
        return List.copyOf(memberships);
    }

    public Long identity() {
        return id;
    }

    public boolean sameAs(final Object other) {
        if (!(other instanceof AirControlArea otherArea)) {
            return false;
        }
        return Objects.equals(this.id, otherArea.id)
                && Objects.equals(this.areaCode, otherArea.areaCode)
                && Objects.equals(this.areaName, otherArea.areaName)
                && Objects.equals(this.minFuelAtArrival, otherArea.minFuelAtArrival)
                && Objects.equals(this.maxAltitude, otherArea.maxAltitude)
                && Objects.equals(this.geoBoundary, otherArea.geoBoundary);
    }

    public AreaCode areaCode() {
        return areaCode;
    }

    public AreaName areaName() {
        return areaName;
    }

    public MinFuelAtArrival minFuelAtArrival() {
        return minFuelAtArrival;
    }

    public MaxAltitude maxAltitude() {
        return maxAltitude;
    }

    public GeoBoundary geoBoundary() {
        return geoBoundary;
    }

    @Override
    public void addMembership(final Membership membership) {
        Preconditions.nonNull(membership, "Membership cannot be null");
        Preconditions.ensure(membership.memberType().equals(MemberType.FCO), "Only FCO members can be added to an air control area.");
        final boolean alreadyExists = hasCollaborator(membership.memberEmail());
        if (alreadyExists) {
            throw new IllegalArgumentException("User is already a collaborator of this air control area.");
        }
        memberships.add(membership);
    }

    @Override
    public String customerReference() {
        return this.areaCode.value();
    }

    @Override
    public String displayName() {
        return this.areaName.value();
    }

    @Override
    public boolean hasCollaborator(final Email email) {
        if (email == null) {
            return false;
        }
        return memberships.stream().anyMatch(m -> m.memberEmail().equals(email));
    }

    @PrePersist
    @PreUpdate
    private void syncPersistenceCoordinatesFromBoundary() {
        this.geoBoundaryCoordinates = List.copyOf(geoBoundary.coordinates());
    }

    @PostLoad
    private void syncBoundaryFromPersistenceCoordinates() {
        if (geoBoundaryCoordinates != null) {
            this.geoBoundary = new GeoBoundary(geoBoundaryCoordinates);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirControlArea that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
