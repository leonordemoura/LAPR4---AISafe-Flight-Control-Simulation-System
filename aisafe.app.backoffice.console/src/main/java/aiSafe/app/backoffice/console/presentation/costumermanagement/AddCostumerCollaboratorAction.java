package aiSafe.app.backoffice.console.presentation.costumermanagement;

import eapli.framework.actions.Action;

/**
 * Action class responsible for handling the addition of a costumer's collaborator in the system. This class will interact with the user interface to gather necessary information about the costume and the collaborator and then delegate the addition process to the appropriate application service.
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
public class AddCostumerCollaboratorAction implements Action {

    @Override
    public boolean execute() {
        return new AddCostumerCollaboratorUI().show();
    }
}
