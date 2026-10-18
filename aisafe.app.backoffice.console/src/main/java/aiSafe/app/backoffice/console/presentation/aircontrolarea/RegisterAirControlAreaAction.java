package aiSafe.app.backoffice.console.presentation.aircontrolarea;

import eapli.framework.actions.Action;

public class RegisterAirControlAreaAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterAirControlAreaUI().show();
    }
}
