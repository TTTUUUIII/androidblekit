package com.outlook.wn123o.blekit.common;

import android.bluetooth.BluetoothGattCharacteristic;

public class BleMsg {
    public final byte[] data;
    public final BluetoothGattCharacteristic characteristic;

    public BleMsg(byte[] data, BluetoothGattCharacteristic characteristic) {
        this.data = data;
        this.characteristic = characteristic;
    }
}
