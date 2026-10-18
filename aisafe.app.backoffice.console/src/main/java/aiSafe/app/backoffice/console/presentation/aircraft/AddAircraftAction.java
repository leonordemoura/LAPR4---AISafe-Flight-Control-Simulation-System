package aiSafe.app.backoffice.console.presentation.aircraft;

import eapli.framework.actions.Action;

public class AddAircraftAction implements Action {
    @Override
    public boolean execute() {
        return new AddAircraftUI().show();
    }
}


