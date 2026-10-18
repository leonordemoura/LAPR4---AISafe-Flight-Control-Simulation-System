package aiSafe.app.backoffice.console.presentation.flight;

import eapli.framework.actions.Action;

public class ValidateFlightPlanAction implements Action {

    @Override
    public boolean execute() {
        return new ValidateFlightPlanUI().show();
    }
}
