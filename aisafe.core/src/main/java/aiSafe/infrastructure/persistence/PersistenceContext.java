package aiSafe.infrastructure.persistence;

import aiSafe.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public final class PersistenceContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceContext.class);

    private static RepositoryFactory theFactory;

    private PersistenceContext() {
        // Utility class.
    }

    public static RepositoryFactory repositories() {
        if (theFactory == null) {
            final String factoryClassName = Application.settings().getRepositoryFactory();
            try {
                theFactory = (RepositoryFactory) Class.forName(factoryClassName).getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                     | IllegalArgumentException | InvocationTargetException | ClassNotFoundException ex) {
                LOGGER.error("Unable to dynamically load the Repository Factory", ex);
                throw new IllegalStateException("Unable to dynamically load the Repository Factory: " + factoryClassName, ex);
            }
        }
        return theFactory;
    }
}
