package com.outlook.wn123o.blekit.util;

import java.nio.ByteBuffer;

public final class ByteUtils {
    private ByteUtils() {}
    public static int asInt(byte[] src) {
        return asInt(src, 0);
    }

    public static int asInt(byte[] src, int offset) {
        return ByteBuffer.wrap(src, offset, Integer.BYTES).getInt();
    }

    public static byte[] asByteArray(int src) {
        return ByteBuffer.allocate(Integer.BYTES)
                .putInt(src)
                .array();
    }
}
