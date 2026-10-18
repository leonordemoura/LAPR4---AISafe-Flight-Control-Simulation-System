package aiSafe.app.backoffice.console.presentation.flightRoute;

import eapli.framework.actions.Action;

/**
 * Action class responsible for handling the deactivation of an existing flight route in the system. This class will interact with the user interface to allow the user to select a flight route to deactivate and then delegate the deactivation process to the appropriate application service.
 * @author 2DE - Group 3
 * @version 2.0
 */
public class DeactivateFlightRouteAction implements Action{

    @Override
    public boolean execute() {
        return new DeactivateFlightRouteUI().show();
    }
}
