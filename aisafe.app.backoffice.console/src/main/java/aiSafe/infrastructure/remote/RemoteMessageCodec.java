package aiSafe.infrastructure.remote;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Length-prefixed UTF-8 message codec for remote TCP access (US044/US078/US086).
 */
public final class RemoteMessageCodec {

    private static final int MAX_PAYLOAD_BYTES = 65536;

    private RemoteMessageCodec() {
    }

    public static String readMessage(final InputStream input) throws IOException {
        final DataInputStream in = new DataInputStream(input);
        final int length = in.readInt();
        if (length <= 0 || length > MAX_PAYLOAD_BYTES) {
            throw new IOException("Invalid message length: " + length);
        }
        final byte[] payload = in.readNBytes(length);
        if (payload.length != length) {
            throw new EOFException("Unexpected end of stream while reading payload");
        }
        return new String(payload, StandardCharsets.UTF_8);
    }

    public static void writeMessage(final OutputStream output, final String message) throws IOException {
        final byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        if (payload.length > MAX_PAYLOAD_BYTES) {
            throw new IOException("Payload too large: " + payload.length);
        }
        final DataOutputStream out = new DataOutputStream(output);
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }
}
