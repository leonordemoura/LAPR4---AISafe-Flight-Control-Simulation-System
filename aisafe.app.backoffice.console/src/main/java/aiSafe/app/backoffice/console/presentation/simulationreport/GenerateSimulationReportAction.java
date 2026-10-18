package aiSafe.app.backoffice.console.presentation.simulationreport;

import eapli.framework.actions.Action;

public class GenerateSimulationReportAction implements Action {

    @Override
    public boolean execute() {
        return new GenerateSimulationReportUI().show();
    }
}
