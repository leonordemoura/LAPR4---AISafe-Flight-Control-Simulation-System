package aiSafe.app.backoffice.console.presentation.flightRoute;

import eapli.framework.actions.Action;

/**
 * Action class responsible for handling the registration of a new flight route in the system. This class will interact with the user interface to gather necessary information about the flight route and then delegate the registration process to the appropriate application service.
 *
 * @author 2DE - Group 3
 * @version 1.0
 */
public class RegisterFlightRouteAction implements Action {

    @Override
    public boolean execute(){
        return new RegisterFlightRouteUI().show();
    }
}
