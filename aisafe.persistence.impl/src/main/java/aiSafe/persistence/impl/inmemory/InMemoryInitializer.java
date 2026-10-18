package aiSafe.persistence.impl.inmemory;

import aiSafe.infrastructure.bootstrap.MasterDataBootstrapper;

/**
 * Initializes default in-memory data once per process.
 * Mirrors the EAPLI example pattern with a lazy singleton initializer.
 */
final class InMemoryInitializer {

    private static class LazyHolder {
        private static final InMemoryInitializer INSTANCE = new InMemoryInitializer();

        private LazyHolder() {
            // utility holder
        }
    }

    private boolean initialized;
    private boolean initializing;

    private InMemoryInitializer() {
        // no public instantiation
    }

    private synchronized void initialize() {
        if (initialized || initializing) {
            return;
        }
        initializing = true;
        try {
            new MasterDataBootstrapper().execute();
            initialized = true;
        } finally {
            initializing = false;
        }
    }

    static void init() {
        LazyHolder.INSTANCE.initialize();
    }
}
