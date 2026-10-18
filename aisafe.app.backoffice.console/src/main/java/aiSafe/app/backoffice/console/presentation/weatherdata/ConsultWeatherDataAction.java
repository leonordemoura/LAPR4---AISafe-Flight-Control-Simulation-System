package aiSafe.app.backoffice.console.presentation.weatherdata;

import eapli.framework.actions.Action;

public class ConsultWeatherDataAction implements Action {
    @Override
    public boolean execute() {
        return new ConsultWeatherDataUI().show();
    }
}
