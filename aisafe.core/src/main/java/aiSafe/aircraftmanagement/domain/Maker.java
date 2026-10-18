package aiSafe.aircraftmanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

@Entity
public class Maker implements AggregateRoot<MakerID> {
    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "makerID"))
    private MakerID makerID;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "MakerName"))
    private MakerName name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "CountryName")),
            @AttributeOverride(name = "numericCode", column = @Column(name = "CountryCode"))
    })
    private Country country;

    protected Maker(){
        // for ORM
    }

    public Maker(final MakerID makerId, final MakerName name, final Country country){
        Preconditions.noneNull(makerId, "Maker ID should neither be null nor empty");
        Preconditions.noneNull(name, "Maker name should neither be null nor empty");
        Preconditions.noneNull(country, "Country should neither be null nor empty");
        this.makerID = makerId;
        this.name = name;
        this.country = country;
    }

    public MakerID makerID() {
        return makerID;
    }

    public MakerName name() {
        return name;
    }

    public Country country() {
        return country;
    }


    @Override
    public int hashCode() {
        return makerID.hashCode();
    }

    @Override
    public boolean sameAs(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        final Maker that = (Maker) other;
        return makerID.equals(that.makerID);
    }

    @Override
    public MakerID identity() {
        return makerID;
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
