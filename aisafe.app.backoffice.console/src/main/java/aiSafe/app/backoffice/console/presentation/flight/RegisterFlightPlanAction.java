package aiSafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class RegisterFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new RegisterFlightPlanUI().show();
    }
}
