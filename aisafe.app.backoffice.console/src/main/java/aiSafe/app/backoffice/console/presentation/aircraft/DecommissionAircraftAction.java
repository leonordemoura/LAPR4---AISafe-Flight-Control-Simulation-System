package aiSafe.app.backoffice.console.presentation.aircraft;

import aiSafe.aircraft.application.DecommissionAircraftController;
import aiSafe.infrastructure.persistence.PersistenceContext;
import eapli.framework.actions.Action;

public class DecommissionAircraftAction implements Action {

    @Override
    public boolean execute() {
        return new DecommissionAircraftUI().show();
    }
}