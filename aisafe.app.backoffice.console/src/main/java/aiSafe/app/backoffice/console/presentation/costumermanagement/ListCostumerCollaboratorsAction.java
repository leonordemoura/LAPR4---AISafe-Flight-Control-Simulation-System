package aiSafe.app.backoffice.console.presentation.costumermanagement;

import eapli.framework.actions.Action;

public class ListCostumerCollaboratorsAction implements Action {
    @Override
    public boolean execute() {
        return new ListCostumerCollaboratorsUI().show();
    }
}
