package aiSafe.app.backoffice.console.presentation.weatherdata;

import eapli.framework.actions.Action;

public class ImportBulkWeatherDataAction implements Action {
    @Override
    public boolean execute() {
        return new ImportBulkWeatherDataUI().show();
    }
}
