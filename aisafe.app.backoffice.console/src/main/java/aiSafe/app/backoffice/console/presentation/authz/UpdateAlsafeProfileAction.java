package aiSafe.app.backoffice.console.presentation.authz;

import eapli.framework.actions.Action;

public class UpdateAlsafeProfileAction implements Action {
    @Override
    public boolean execute() {
        return new UpdateAlsafeProfileUI().show();
    }
}

