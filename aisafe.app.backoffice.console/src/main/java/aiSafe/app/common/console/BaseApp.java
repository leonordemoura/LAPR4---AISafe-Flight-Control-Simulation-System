package aiSafe.app.common.console;

import aiSafe.Application;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import eapli.framework.infrastructure.pubsub.impl.inprocess.service.InProcessPubSub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("squid:S106")
public abstract class BaseApp {
    protected static final String SEPARATOR_HR = "=====================================";

    private static final Logger LOGGER = LogManager.getLogger(BaseApp.class);

    final EventDispatcher dispatcher = InProcessPubSub.dispatcher();

    public void run(final String[] args) {
        printHeader();
        try {
            setupEventHandlers();
            doMain(args);
            printFooter();
        } catch (final Exception e) {
            System.out.println("Something unexpected has happened and the application will terminate. Please check the logs.\n");
            LOGGER.error(e);
        } finally {
            clearEventHandlers();
        }

        System.exit(0);
    }

    protected void printFooter() {
        System.out.println("\n");
        System.out.println(SEPARATOR_HR);
        System.out.println(appGoodbye());
        System.out.println(SEPARATOR_HR);
    }

    protected void printHeader() {
        System.out.println(SEPARATOR_HR);
        System.out.println(appTitle() + " " + Application.VERSION);
        System.out.println(Application.COPYRIGHT);
        System.out.println(SEPARATOR_HR);
    }

    private void clearEventHandlers() {
        try {
            doClearEventHandlers(dispatcher);
            dispatcher.shutdown();
        } catch (final Exception e) {
            LOGGER.error("Unable to cleanup event handlers", e);
        }
    }

    private void setupEventHandlers() {
        doSetupEventHandlers(dispatcher);
    }

    protected abstract void doMain(String[] args);

    protected abstract String appTitle();

    protected abstract String appGoodbye();

    protected void doClearEventHandlers(final EventDispatcher dispatcher) {
        // no-op by default
    }

    protected abstract void doSetupEventHandlers(EventDispatcher dispatcher);
}
