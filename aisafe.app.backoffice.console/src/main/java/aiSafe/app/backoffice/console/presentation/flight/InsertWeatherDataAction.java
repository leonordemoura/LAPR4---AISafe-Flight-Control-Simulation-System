package aiSafe.app.backoffice.console.presentation.flight;

import aiSafe.app.backoffice.console.presentation.aircraftmanagement.RegisterEngineModelUI;
import eapli.framework.actions.Action;

public class InsertWeatherDataAction implements Action{

    @Override
    public boolean execute() {
        return new InsertWeatherDataUI().show();
    }
}
