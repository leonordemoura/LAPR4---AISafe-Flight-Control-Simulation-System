package aiSafe.app.backoffice.console.presentation.reportGeneration;

import eapli.framework.actions.Action;

/**
 * Action class responsible for initiating the report generation process.
 * This class implements the Action interface and serves as a bridge between the user interface and the underlying report generation logic.
 * @author 2DE - Group 3
 * @version 2.0
 */
public class GenerateReportAction implements Action {

    /**
     * Executes the action to generate a report.
     * @return true if the report generation process was initiated successfully, false otherwise.
     */
    @Override
    public boolean execute() {
        return new GenerateReportUI().show();
    }
}
