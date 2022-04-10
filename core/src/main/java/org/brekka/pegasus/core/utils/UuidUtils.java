package org.brekka.pegasus.core.utils;

import static java.lang.String.format;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class UuidUtils {

    private UuidUtils() {
    }

    public static byte[] toBytes(final UUID id) {
        if (id == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(id.getMostSignificantBits());
        buffer.putLong(id.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID fromBytes(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException(format("Expecting 16 bytes, found %s", bytes.length));
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
