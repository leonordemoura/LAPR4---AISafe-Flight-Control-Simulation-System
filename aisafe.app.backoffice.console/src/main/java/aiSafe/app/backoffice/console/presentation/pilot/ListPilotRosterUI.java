package aiSafe.app.backoffice.console.presentation.pilot;

import aiSafe.PilotCertification.application.ListPilotRosterController;
import aiSafe.PilotCertification.domain.dto.PilotRosterDTO;

import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ListPilotRosterUI extends AbstractUI {

    private final ListPilotRosterController controller =
            new ListPilotRosterController();

    @Override
    protected boolean doShow() {

        try {

            final List<PilotRosterDTO> roster =
                    controller.listRoster();

            // =====================================================
            // EMPTY
            // =====================================================

            if (roster.isEmpty()) {

                System.out.println();
                System.out.println(
                        "No pilots registered for your company."
                );

                return false;
            }

            // =====================================================
            // HEADER
            // =====================================================

            System.out.println();
            System.out.println(
                    "================ PILOT ROSTER ================"
            );

            // =====================================================
            // LIST
            // =====================================================

            for (PilotRosterDTO pilot : roster) {

                System.out.println();
                System.out.println(
                        "Pilot ID : " + pilot.pilotId()
                );

                System.out.println(
                        "Email    : " + pilot.email()
                );

                System.out.println(
                        "Status   : "
                                + (pilot.active()
                                ? "ACTIVE"
                                : "INACTIVE")
                );

                System.out.println(
                        "Certified Aircraft Models:"
                );

                for (String model :
                        pilot.certifiedAircraftModels()) {

                    System.out.println(" - " + model);
                }

                System.out.println(
                        "------------------------------------------------"
                );
            }

        } catch (Exception e) {

            System.out.println();
            System.out.println(
                    "Error listing pilot roster: "
                            + e.getMessage()
            );
        }

        return false;
    }

    @Override
    public String headline() {

        return "List Pilot Roster";
    }
}