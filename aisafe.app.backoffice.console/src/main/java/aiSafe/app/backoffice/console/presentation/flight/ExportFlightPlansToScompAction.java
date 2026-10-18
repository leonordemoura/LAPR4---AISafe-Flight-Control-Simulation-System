package aiSafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class ExportFlightPlansToScompAction implements Action {

    @Override
    public boolean execute() {
        return new ExportFlightPlansToScompUI().show();
    }
}
