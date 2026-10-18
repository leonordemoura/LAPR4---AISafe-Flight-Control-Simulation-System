package aiSafe.app.backoffice.console.presentation.aircraftModel;

import aiSafe.app.backoffice.console.presentation.aircontrolarea.RegisterAirControlAreaUI;
import eapli.framework.actions.Action;

public class RegisterAircraftModelAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterAircraftModelUI().show();
    }
}
