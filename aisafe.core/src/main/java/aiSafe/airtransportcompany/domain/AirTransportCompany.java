package aiSafe.airtransportcompany.domain;

import aiSafe.aircraft.domain.Aircraft;
import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.Costumer;
import aiSafe.usermanagement.domain.Email;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "AirTransportCompany",
        uniqueConstraints = @UniqueConstraint(columnNames = {"companyName", "iataCode", "icaoCode"}))
public class AirTransportCompany implements AggregateRoot<Long>, Costumer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "companyID")
    private Long companyID;

    @Embedded
    private CompanyName companyName;

    @Embedded
    private IataCode iataCode;

    @Embedded
    private IcaoCode icaoCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "air_transport_company_memberships",
            joinColumns = @JoinColumn(name = "company_id", referencedColumnName = "companyID")
    )
    private List<Membership> memberships;

    protected AirTransportCompany() {
        // for ORM
    }

    public AirTransportCompany(final CompanyName companyName, final IataCode iataCode, final IcaoCode icaoCode) {
        Preconditions.nonEmpty(companyName.name(), "Company name cannot be null or empty.");
        Preconditions.nonEmpty(iataCode.code(), "IATA code cannot be null or empty.");
        Preconditions.nonEmpty(icaoCode.code(), "ICAO code cannot be null or empty.");
        this.companyName = companyName;
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.memberships = new ArrayList<>();
    }

    public CompanyName companyName() {
        return this.companyName;
    }

    public IataCode iataCode() {
        return this.iataCode;
    }

    public IcaoCode icaoCode() {
        return this.icaoCode;
    }

    public List<Membership> memberships() {
        return this.memberships;
    }

    @OneToMany(mappedBy = "company")
    private List<Aircraft> fleet = new ArrayList<>();

    public void addAircraft(final Aircraft aircraft) {
        Preconditions.nonNull(aircraft, "Aircraft cannot be null");
        this.fleet.add(aircraft);
    }

    @Override
    public void addMembership(final Membership membership) {
        Preconditions.nonNull(membership, "Membership cannot be null");
        Preconditions.ensure(membership.memberType().equals(MemberType.PILOT) || membership.memberType().equals(MemberType.ATCC), "Only pilots or ATCC members can be added as collaborators to an air transport company.");
        final boolean alreadyExists = hasCollaborator(membership.memberEmail());
        if (alreadyExists) {
            throw new IllegalArgumentException("User is already a collaborator of this air transport company.");
        }
        this.memberships.add(membership);
    }

    @Override
    public String customerReference() {
        return this.iataCode.code();
    }

    @Override
    public String displayName() {
        return this.companyName.name();
    }

    @Override
    public boolean hasCollaborator(final Email email) {
        if (email == null) {
            return false;
        }
        return memberships.stream().anyMatch(m -> m.memberEmail().equals(email));
    }

    public boolean hasActivePilotMember(final Email email) {
        if (email == null) {
            return false;
        }
        return memberships.stream()
                .anyMatch(m -> m.memberEmail().equals(email)
                        && m.memberType() == MemberType.PILOT
                        && m.isActive());
    }

    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        final AirTransportCompany that = (AirTransportCompany) other;
        return companyName.equals(that.companyName) || iataCode.equals(that.iataCode) || icaoCode.equals(that.icaoCode);
    }

    @Override
    public Long identity() {
        return this.companyID;
    }
}
