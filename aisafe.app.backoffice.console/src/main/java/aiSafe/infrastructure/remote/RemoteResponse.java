package aiSafe.infrastructure.remote;

public final class RemoteResponse {

    private RemoteResponse() {
    }

    public static String ok(final String status, final String payload) {
        if (payload == null || payload.isEmpty()) {
            return "OK;" + status;
        }
        return "OK;" + status + ";" + payload;
    }

    public static String error(final String code, final String message) {
        return "ERR;" + code + ";" + sanitize(message);
    }

    private static String sanitize(final String message) {
        if (message == null) {
            return "";
        }
        return message.replace(';', ',').replace('\n', ' ').trim();
    }
}
