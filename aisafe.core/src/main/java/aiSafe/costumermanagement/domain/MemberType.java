package aiSafe.costumermanagement.domain;

public enum MemberType {
    ATCC ("AIR_TRANSPORT_COMPANY_COLLABORATOR"),
    PILOT ("Pilot"),
    FCO ("FLIGHT_CONTROL_OPERATOR");;

    private final String description;

     MemberType(String description){
        this.description = description;
    }

    @Override
    public String toString(){
         return description;
    }
}
