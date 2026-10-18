package aiSafe.app.backoffice.console.presentation.costumermanagement;

import aiSafe.aircontrolarea.domain.AirControlArea;
import aiSafe.airtransportcompany.domain.AirTransportCompany;
import aiSafe.costumermanagement.application.ListCostumerCollaboratorsController;
import aiSafe.costumermanagement.application.ListCostumerCollaboratorsController.ActiveCollaborator;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

public class ListCostumerCollaboratorsUI extends AbstractUI {
    private static final int NAME_WIDTH = 30;
    private static final int EMAIL_WIDTH = 35;
    private static final int POSITION_WIDTH = 20;

    private final ListCostumerCollaboratorsController theController;

    public ListCostumerCollaboratorsUI() {
        this.theController = new ListCostumerCollaboratorsController();
    }

    @Override
    protected boolean doShow() {
        final Integer option = chooseCostumerType();

        try {
            if (option == 1) {
                handleCompanyFlow();
            } else {
                handleAreaFlow();
            }
        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to list collaborators: " + ex.getMessage());
            return false;
        }

        return false;
    }

    private void handleCompanyFlow() {
        final List<AirTransportCompany> companies = theController.getAirTransportCompanies();

        if (companies.isEmpty()) {
            System.out.println("No companies registered.");
            return;
        }

        System.out.println("\nAvailable Air Transport Companies:");
        for (int i = 0; i < companies.size(); i++) {
            final AirTransportCompany company = companies.get(i);
            System.out.printf("%d - %s (%s)%n", i + 1, company.companyName().name(), company.iataCode().code());
        }

        final int selected = Console.readInteger("Select company:");
        if (selected < 1 || selected > companies.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        final AirTransportCompany company = companies.get(selected - 1);
        final List<ActiveCollaborator> collaborators =
                theController.listActiveCollaboratorsByCompany(company.iataCode().code());

        displayCollaborators(collaborators);
    }

    private void handleAreaFlow() {
        final List<AirControlArea> areas = theController.getAirControlAreas();

        if (areas.isEmpty()) {
            System.out.println("No areas registered.");
            return;
        }

        System.out.println("\nAvailable Air Control Areas:");
        for (int i = 0; i < areas.size(); i++) {
            final AirControlArea area = areas.get(i);
            System.out.printf("%d - %s (%s)%n", i + 1, area.areaName().value(), area.areaCode().value());
        }

        final int selected = Console.readInteger("Select area:");
        if (selected < 1 || selected > areas.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        final AirControlArea area = areas.get(selected - 1);
        final List<ActiveCollaborator> collaborators =
                theController.listActiveCollaboratorsByArea(area.areaCode().value());

        displayCollaborators(collaborators);
    }

    private void displayCollaborators(final List<ActiveCollaborator> collaborators) {
        if (collaborators.isEmpty()) {
            System.out.println("No active collaborators.");
            return;
        }

        System.out.printf("%nFound %d active collaborator(s):%n%n", collaborators.size());
        System.out.printf("%-" + NAME_WIDTH + "s | %-" + EMAIL_WIDTH + "s | %-" + POSITION_WIDTH + "s%n",
                "Name", "Email", "Position");
        System.out.println("-".repeat(NAME_WIDTH + EMAIL_WIDTH + POSITION_WIDTH + 6));

        for (ActiveCollaborator row : collaborators) {
            System.out.printf("%-" + NAME_WIDTH + "s | %-" + EMAIL_WIDTH + "s | %-" + POSITION_WIDTH + "s%n",
                    row.user().fullName().toString(),
                    row.user().email().value(),
                    row.membership().position().position());
        }
    }

    private Integer chooseCostumerType() {
        Integer response;
        while (true) {
            System.out.println("Costumer type:");
            System.out.println("1 - Air Transport Company");
            System.out.println("2 - Air Control Area");
            response = Console.readInteger("Please enter your answer");
            if (response >= 1 && response <= 2) {
                return response;
            }
            System.out.println("Please enter a number between 1 and 2!");
        }
    }

    @Override
    public String headline() {
        return "List Costumer Collaborators";
    }
}
