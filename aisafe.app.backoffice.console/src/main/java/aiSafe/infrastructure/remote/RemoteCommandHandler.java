package aiSafe.infrastructure.remote;

public interface RemoteCommandHandler {
    String handle(String commandLine, RemoteConnectionContext context);

    String serviceId();
}

