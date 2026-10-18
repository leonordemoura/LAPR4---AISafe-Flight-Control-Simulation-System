package aiSafe.app.backoffice.console.presentation.weatherdata;

import eapli.framework.actions.Action;

public class RegisterWeatherDataAction implements Action {
    @Override
    public boolean execute() {
        return new RegisterWeatherDataUI().show();
    }
}
