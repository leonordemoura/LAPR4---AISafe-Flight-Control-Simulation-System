package aiSafe.app.backoffice.console.presentation.aircraft;

import eapli.framework.actions.Action;

public class ListFleetAction implements Action {
    @Override
    public boolean execute() {
        return new ListFleetUI().show();
    }
}


