package aiSafe.costumermanagement.application;

import eapli.framework.infrastructure.authz.domain.model.Role;
import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.costumermanagement.domain.Membership;
import aiSafe.costumermanagement.domain.MemberType;
import aiSafe.costumermanagement.domain.Costumer;
import aiSafe.costumermanagement.domain.Position;
import aiSafe.usermanagement.domain.Email;
import aiSafe.usermanagement.domain.User;

public class MembershipFactory {

    public void createMembership(final Costumer costumer, final User user, final Position position){
        if (costumer instanceof AirTransportCompany company) {
            Membership companyMembership = createAtcMembership(user.email(), user.assignedRole(), position);
            company.addMembership(companyMembership);
        } else if (costumer instanceof AirControlArea area) {
            Membership areaMembership = createAreaMembership(user.email(), user.assignedRole(), position);
            area.addMembership(areaMembership);
        } else {
            throw new IllegalArgumentException("Unknown customer type.");
        }
    }

    public Membership createAtcMembership(final Email email, final Role role, final Position position) {
        boolean isValid = role.toString().equalsIgnoreCase(MemberType.ATCC.toString()) ||
                role.toString().equalsIgnoreCase(MemberType.PILOT.toString());
        if (!isValid) {
            throw new IllegalArgumentException("User role is not compatible with ATC membership.");
        }
        MemberType memberType = role.toString().equalsIgnoreCase(MemberType.ATCC.toString())
                ? MemberType.ATCC
                : MemberType.PILOT;
        return new Membership(email, position, memberType);
    }

    public Membership createAreaMembership(final Email email, final Role role, final Position position) {
        if (!role.toString().equalsIgnoreCase(MemberType.FCO.toString())){
            throw new IllegalArgumentException("User role is not compatible with FCO membership.");
        }
        return new Membership(email, position, MemberType.FCO);
    }
}
