package aiSafe.infrastructure.remote;

import aiSafe.Application;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Sends remote access events to the US090 logging server (UDP).
 */
public final class RemoteAccessUdpLogger {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_INSTANT;

    private final String host;
    private final int port;

    public RemoteAccessUdpLogger() {
        this.host = Application.settings().getProperty("remote.udp.logging.host");
        this.port = parsePort(Application.settings().getProperty("remote.udp.logging.port"), 5000);
    }

    public RemoteAccessUdpLogger(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public void log(final String username, final String clientIp, final int clientPort,
                    final String serviceId, final String event) {
        if (host == null || host.isBlank()) {
            return;
        }
        final String safeUser = username == null ? "-" : username;
        final String payload = TIMESTAMP_FORMAT.format(Instant.now()) + ";"
                + safeUser + ";" + clientIp + ";" + clientPort + ";" + serviceId + ";" + event;

        try (DatagramSocket socket = new DatagramSocket()) {
            final byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            final InetAddress address = InetAddress.getByName(host);
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(packet);
        } catch (final Exception ex) {
            System.err.println("[US090] Failed to send UDP log: " + ex.getMessage());
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
