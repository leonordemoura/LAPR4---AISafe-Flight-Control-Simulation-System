package aiSafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class ImportFlightPlanFromFileAction implements Action {
        public boolean execute() {
            return new ImportFlightPlanFromFileUI().show();
        }
}
