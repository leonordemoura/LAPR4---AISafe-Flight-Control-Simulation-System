package aiSafe.PilotCertification.domain.dto;

import java.util.List;

public class PilotRosterDTO {

    private final String pilotId;
    private final String email;
    private final boolean active;
    private final List<String> certifiedAircraftModels;

    public PilotRosterDTO(
            final String pilotId,
            final String email,
            final boolean active,
            final List<String> certifiedAircraftModels
    ) {
        this.pilotId = pilotId;
        this.email = email;
        this.active = active;
        this.certifiedAircraftModels = certifiedAircraftModels;
    }

    public String pilotId() {
        return pilotId;
    }

    public String email() {
        return email;
    }

    public boolean active() {
        return active;
    }

    public List<String> certifiedAircraftModels() {
        return certifiedAircraftModels;
    }
}