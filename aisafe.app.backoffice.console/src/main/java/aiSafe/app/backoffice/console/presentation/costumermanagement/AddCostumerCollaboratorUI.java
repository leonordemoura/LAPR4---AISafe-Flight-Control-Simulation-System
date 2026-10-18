package aiSafe.app.backoffice.console.presentation.costumermanagement;

import aiSafe.costumermanagement.application.AddCostumerCollaboratorController;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.io.util.Console;


/**
 * User interface class responsible for facilitating the addition of a new costumer collaborator in the system. This class will interact with the user to gather necessary information about the engine model and then delegate the registration process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 1.0
 */
public class AddCostumerCollaboratorUI extends AbstractUI {
    private final AddCostumerCollaboratorController theController;

    public AddCostumerCollaboratorUI(){
        theController = new AddCostumerCollaboratorController();
    }

    @Override
    protected boolean doShow() {
        final Integer option = chooseCostumer();
        final String identifier = Console.readNonEmptyLine("Enter costumer unique identifier:", "Answer cannot be null or empty");
        final String email = Console.readNonEmptyLine("Enter collaborator email: ", "Answer cannot be null or empty");
        final String position = Console.readNonEmptyLine("Enter collaborator position: ", "Answer cannot be null or empty");

        try {
            theController.registerCostumerCollaborator(option, identifier, email, position);
        } catch (final IllegalArgumentException ex) {
            System.out.println("Unable to register collaborator: " + ex.getMessage());
            return false;
        }
        System.out.printf("Costumer added with success: %s with %s position\n", email, position);
        return true;
    }

    private Integer chooseCostumer(){
        Integer response;
        while (true) {
            System.out.println("Costumer type:");
            System.out.println("1 - Air Transport Company");
            System.out.println("2 - Air Control Area");
            response = Console.readInteger("Please enter your answer");
            if (response >= 1 && response <= 2){
                return response;
            }
            System.out.println("Please enter a number between 1 and 2!");
        }
    }

    @Override
    public String headline() {
        return "Add New Costumer Collaborator";
    }
}
