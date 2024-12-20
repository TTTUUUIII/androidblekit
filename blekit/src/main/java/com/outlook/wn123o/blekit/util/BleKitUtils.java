package com.outlook.wn123o.blekit.util;

import java.util.UUID;

public final class BleKitUtils {
    private BleKitUtils() {
        throw new RuntimeException();
    }

    public static UUID ble16BitUuid(int d) {
        return new UUID(0x1000L |  ((long) d << 32), 0x800000805f9B34FBL);
    }
}
