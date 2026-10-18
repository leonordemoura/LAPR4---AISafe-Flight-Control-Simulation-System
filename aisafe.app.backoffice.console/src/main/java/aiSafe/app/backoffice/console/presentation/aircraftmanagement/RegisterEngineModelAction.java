package aiSafe.app.backoffice.console.presentation.aircraftmanagement;

import eapli.framework.actions.Action;

/**
 * Action class responsible for handling the registration of a new engine model in the system. This class will interact with the user interface to gather necessary information about the engine model and then delegate the registration process to the appropriate application service.
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
public class RegisterEngineModelAction implements Action{

    @Override
    public boolean execute() {
        return new RegisterEngineModelUI().show();
    }
}
