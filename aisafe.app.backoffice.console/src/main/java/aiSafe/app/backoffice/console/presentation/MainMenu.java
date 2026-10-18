package aiSafe.app.backoffice.console.presentation;

import aiSafe.Application;
import aiSafe.app.backoffice.console.presentation.aircraft.AddAircraftAction;
import aiSafe.app.backoffice.console.presentation.airTransportCompany.RegisterAirTransportCompanyAction;
import aiSafe.app.backoffice.console.presentation.aircontrolarea.RegisterAirControlAreaAction;
import aiSafe.app.backoffice.console.presentation.aircraft.DecommissionAircraftAction;
import aiSafe.app.backoffice.console.presentation.aircraft.ListFleetAction;
import aiSafe.app.backoffice.console.presentation.aircraftModel.RegisterAircraftModelAction;
import aiSafe.app.backoffice.console.presentation.aircraftmanagement.RegisterEngineModelAction;
import aiSafe.app.backoffice.console.presentation.costumermanagement.AddCostumerCollaboratorAction;
import aiSafe.app.backoffice.console.presentation.costumermanagement.ListCostumerCollaboratorsAction;
import aiSafe.app.backoffice.console.presentation.airport.RegisterAirportAction;
import aiSafe.app.backoffice.console.presentation.authz.MyUserMenu;
import aiSafe.app.backoffice.console.presentation.authz.RegisterUserAction;
import aiSafe.app.backoffice.console.presentation.authz.UpdateAlsafeProfileAction;
import aiSafe.app.backoffice.console.presentation.flight.*;
import aiSafe.app.backoffice.console.presentation.flightRoute.DeactivateFlightRouteAction;
import aiSafe.app.backoffice.console.presentation.pilot.RemovePilotAction;
import aiSafe.app.backoffice.console.presentation.reportGeneration.GenerateReportAction;
import aiSafe.app.backoffice.console.presentation.simulationreport.GenerateSimulationReportAction;
import aiSafe.app.backoffice.console.presentation.flightRoute.RegisterFlightRouteAction;
import aiSafe.app.backoffice.console.presentation.pilot.ListPilotRosterAction;
import aiSafe.app.backoffice.console.presentation.weatherdata.ConsultWeatherDataAction;
import aiSafe.app.backoffice.console.presentation.weatherdata.ImportBulkWeatherDataAction;
import aiSafe.app.backoffice.console.presentation.weatherdata.RegisterWeatherDataAction;
import aiSafe.app.backoffice.console.presentation.pilot.AddPilotAction;
import aiSafe.usermanagement.application.AuthorizationGuardService;
import aiSafe.usermanagement.domain.AisafeRoles;
import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.ExitWithMessageAction;
import eapli.framework.presentation.console.ShowMessageAction;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class MainMenu extends AbstractUI {

    private static final int EXIT_OPTION = 0;
    private static final int ACCOUNT_OPTION = 1;
    private static final int OPERATIONS_OPTION = 2;
    private static final int ADMIN_OPTION = 3;

    private final AuthorizationService authz =
            AuthzRegistry.authorizationService();

    private final AuthorizationGuardService authorizationGuard =
            new AuthorizationGuardService();

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    @Override
    protected boolean doShow() {
        final Menu menu = buildMainMenu();

        final MenuRenderer renderer =
                Application.settings().isMenuLayoutHorizontal()
                        ? new HorizontalMenuRenderer(menu, MenuItemRenderer.DEFAULT)
                        : new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);

        return renderer.render();
    }

    @Override
    public String headline() {
        return authz.session()
                .map(s -> "Backoffice [ @" + s.authenticatedUser().identity() + " ]")
                .orElse("Backoffice [ ==Anonymous== ]");
    }

    private Menu buildMainMenu() {

        final Menu menu = new Menu();

        // ---------------- ACCOUNT ----------------
        menu.addSubMenu(ACCOUNT_OPTION, new MyUserMenu());

        // ---------------- OPERATIONS ----------------
        final Menu operationsMenu = new Menu("Operations >");
        int op = 1;

        if (authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.BACKOFFICE_OPERATOR)) {
            operationsMenu.addItem(op++, "Register air control area", new RegisterAirControlAreaAction());
            operationsMenu.addItem(op++, "Register airport", new RegisterAirportAction());
            operationsMenu.addItem(op++, "Register Air Transport Company", new RegisterAirTransportCompanyAction());
            operationsMenu.addItem(op++, "Add customer collaborator", new AddCostumerCollaboratorAction());
            operationsMenu.addItem(op++, "List customer collaborators", new ListCostumerCollaboratorsAction());
        }

        if(authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.PILOT)){
            operationsMenu.addItem(op++, "Register flight plan", new RegisterFlightPlanAction());
            operationsMenu.addItem(op++, "Import flight plan from file", new ImportFlightPlanFromFileAction());
            operationsMenu.addItem(op++, "Validate flight plan", new ValidateFlightPlanAction());
            operationsMenu.addItem(op++, "Export flight plans to SCOMP", new ExportFlightPlansToScompAction());
            operationsMenu.addItem(op++, "Consult weather data", new ConsultWeatherDataAction());
            operationsMenu.addItem(op++, "Insert weather data in a flight", new InsertWeatherDataAction());

        }

        if (authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.FLIGHT_CONTROL_OPERATOR)) {
            operationsMenu.addItem(op++, "Generate simulation report", new GenerateSimulationReportAction());
            operationsMenu.addItem(op++, "Consult weather data", new ConsultWeatherDataAction());
            operationsMenu.addItem(op++, "Generate monthly statistics report", new GenerateReportAction());

        }

        if (authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.WEATHER_PERSON, AisafeRoles.ADMIN)) {
            operationsMenu.addItem(op++, "Register weather data", new RegisterWeatherDataAction());
            operationsMenu.addItem(op++, "Consult weather data", new ConsultWeatherDataAction());
            operationsMenu.addItem(op++, "Import bulk weather data", new ImportBulkWeatherDataAction());

        }

        if(authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)){
            operationsMenu.addItem(op++, "Register flight route", new RegisterFlightRouteAction());
            operationsMenu.addItem(op++, "Deactivate flight route", new DeactivateFlightRouteAction());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR,
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        )) {
            operationsMenu.addSubMenu(op++, buildAircraftManagementMenu());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.PILOT)) {
            operationsMenu.addItem(op++, "Register flight", new RegisterFlightAction());
        }

        operationsMenu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);

        if (authz.isAuthenticatedUserAuthorizedTo(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR,
                AisafeRoles.WEATHER_PERSON,
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AisafeRoles.PILOT,
                AisafeRoles.FLIGHT_CONTROL_OPERATOR
        )) {
            menu.addSubMenu(OPERATIONS_OPTION, operationsMenu);
        } else {
            menu.addItem(MenuItem.separator("--------------"));
            menu.addItem(OPERATIONS_OPTION,
                    "Operations unavailable",
                    new ShowMessageAction("Current user has no backoffice role."));
        }

        // ---------------- ADMIN ----------------
        if (authz.isAuthenticatedUserAuthorizedTo(AisafeRoles.ADMIN)) {
            menu.addSubMenu(ADMIN_OPTION, buildAdminMenu());
        }

        // ---------------- EXIT ----------------
        menu.addItem(EXIT_OPTION, "Exit", new ExitWithMessageAction("Bye, Bye"));

        return menu;
    }

    private Menu buildAircraftManagementMenu() {

        final Menu menu = new Menu("Aircraft Management");
        int op = 1;

        if (authz.isAuthenticatedUserAuthorizedTo(
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR
        )) {
            menu.addItem(op++, "Register Engine Model", new RegisterEngineModelAction());
            menu.addItem(op++, "Register Aircraft Model", new RegisterAircraftModelAction());
        }

        if (authz.isAuthenticatedUserAuthorizedTo(
                AisafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AisafeRoles.ADMIN,
                AisafeRoles.BACKOFFICE_OPERATOR
        )) {
            menu.addItem(op++, "Add Aircraft to Fleet", new AddAircraftAction());
            menu.addItem(op++, "Decommission Aircraft", new DecommissionAircraftAction());
            menu.addItem(op++, "List an air transport company's fleet", new ListFleetAction());
            menu.addItem(op++, "Add Pilot", new AddPilotAction());
            menu.addItem(op++, "List pilot roster", new ListPilotRosterAction());
            menu.addItem(op++, "Remove pilot", new RemovePilotAction());
        }

        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);

        return menu;
    }

    private Menu buildAdminMenu() {

        final Menu menu = new Menu("Administration");
        int op = 1;

        menu.addItem(op++, "Register backoffice user", new RegisterUserAction());
        menu.addItem(op++, "Update Aisafe Profile", new UpdateAlsafeProfileAction());

        menu.addItem(EXIT_OPTION, "Return", Actions.SUCCESS);

        return menu;
    }

    private boolean runProtectedOperation() {
        authorizationGuard.ensureAuthenticatedBackofficeUser();
        return new ShowMessageAction(
                "Protected operation executed with valid authorization."
        ).execute();
    }
}