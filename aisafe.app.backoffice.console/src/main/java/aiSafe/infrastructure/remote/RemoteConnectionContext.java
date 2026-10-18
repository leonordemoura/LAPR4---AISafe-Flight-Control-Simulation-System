package aiSafe.infrastructure.remote;

public final class RemoteConnectionContext {

    private final String clientIp;
    private final int clientPort;
    private final RemoteAccessUdpLogger udpLogger;
    private boolean authenticated;
    private String username;
    private String role;
    private String serviceId;

    public RemoteConnectionContext(final String clientIp, final int clientPort, final RemoteAccessUdpLogger udpLogger) {
        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.udpLogger = udpLogger;
    }

    public String clientIp() {
        return clientIp;
    }

    public int clientPort() {
        return clientPort;
    }

    public RemoteAccessUdpLogger udpLogger() {
        return udpLogger;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String username() {
        return username;
    }

    public String role() {
        return role;
    }

    public String serviceId() {
        return serviceId != null ? serviceId : "UNKNOWN";
    }

    /** Used by US044/US086 handlers (role name + US090 service id). */
    public void setAuthenticated(final String username, final String role, final String serviceId) {
        this.authenticated = true;
        this.username = username;
        this.role = role;
        this.serviceId = serviceId;
    }

    public void clearAuthentication() {
        this.authenticated = false;
        this.username = null;
        this.role = null;
        this.serviceId = null;
    }
}
