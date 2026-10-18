package aiSafe.infrastructure.remote;

import aiSafe.Application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RemoteAccessTcpServer implements AutoCloseable {

    private final int port;
    private final RemoteCommandHandler commandHandler;
    private final RemoteAccessUdpLogger udpLogger;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private ExecutorService acceptExecutor;
    private ExecutorService clientExecutor;

    public RemoteAccessTcpServer() {
        this(parsePort(Application.settings().getProperty("remote.tcp.port"), 9000));
    }

    public RemoteAccessTcpServer(final int port) {
        this.port = port;
        this.commandHandler = new CompositeRemoteCommandHandler();
        this.udpLogger = new RemoteAccessUdpLogger();
    }

    public void start() throws IOException {
        if (running.get()) {
            return;
        }
        serverSocket = new ServerSocket(port);
        acceptExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "remote-tcp-accept"));
        clientExecutor = Executors.newCachedThreadPool(r -> new Thread(r, "remote-tcp-client"));
        running.set(true);

        acceptExecutor.submit(this::acceptLoop);
        System.out.println("[Remote] Remote TCP server listening on port " + port);
    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                final Socket client = serverSocket.accept();
                clientExecutor.submit(new RemoteConnectionHandler(client, commandHandler, udpLogger));
            } catch (final IOException ex) {
                if (running.get()) {
                    System.err.println("[Remote] Accept error: " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (final IOException ignored) {
                // ignore shutdown errors
            }
        }
        if (acceptExecutor != null) {
            acceptExecutor.shutdownNow();
        }
        if (clientExecutor != null) {
            clientExecutor.shutdownNow();
        }
    }

    private static int parsePort(final String value, final int defaultPort) {
        if (value == null || value.isBlank()) {
            return defaultPort;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (final NumberFormatException ex) {
            return defaultPort;
        }
    }
}
