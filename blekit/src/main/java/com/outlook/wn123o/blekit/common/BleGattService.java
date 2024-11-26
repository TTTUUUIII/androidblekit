package com.outlook.wn123o.blekit.common;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.outlook.wn123o.blekit.BleEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class BleGattService {
    public final UUID uuid;
    private final Collection<UUID> uuidsForNotification;
    private final Collection<UUID> uuidsForWritable;
    private final Collection<UUID> uuidsForWritableNoResponse;
    public final List<BluetoothGattCharacteristic> characteristicsForNotification = new ArrayList<>();
    public final List<BluetoothGattCharacteristic> characteristicsForWritable = new ArrayList<>();
    public final List<BluetoothGattCharacteristic> characteristicsForWritableNoResponse = new ArrayList<>();
    public final BluetoothGattService service;

    private BleGattService(UUID uuid, Collection<UUID> uuidsForNotification, Collection<UUID> uuidsForWritable, Collection<UUID> uuidsForWritableNoResponse) {
        this.uuid = uuid;
        this.uuidsForWritable = uuidsForWritable;
        this.uuidsForNotification = uuidsForNotification;
        this.uuidsForWritableNoResponse = uuidsForWritableNoResponse;
        service = new BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        generateCharacteristics();
        characteristicsForWritable.forEach(service::addCharacteristic);
        characteristicsForNotification.forEach(service::addCharacteristic);
        characteristicsForWritableNoResponse.forEach(service::addCharacteristic);
    }

    public static class Builder {
        private final UUID uuid;
        public final List<UUID> uuidsForNotification = new ArrayList<>();
        public final List<UUID> uuidsForWritable = new ArrayList<>();
        public final List<UUID> uuidsForWritableNoResponse = new ArrayList<>();

        public Builder(UUID uuid) {
            this.uuid = uuid;
        }

        public void addUuidForNotification(UUID uuid) {
            uuidsForNotification.add(uuid);
        }

        public void addUuidForWritable(UUID uuid) {
            uuidsForWritable.add(uuid);
        }

        public void addUuidForWritableNoResponse(UUID uuid) {
            uuidsForWritableNoResponse.add(uuid);
        }

        public BleGattService build() {
            return new BleGattService(uuid, uuidsForNotification, uuidsForWritable, uuidsForWritableNoResponse);
        }
    }

    private void generateCharacteristics() {
        uuidsForNotification.forEach(uuid -> {
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    uuid,
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            characteristic.addDescriptor(BleEnvironment.notificationDescriptor);
            characteristicsForNotification.add(characteristic);
        });
        uuidsForWritable.forEach(uuid -> {
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    uuid,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE
            );
            characteristicsForWritable.add(characteristic);
        });
        uuidsForWritableNoResponse.forEach(uuid -> {
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    uuid,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE
            );
            characteristicsForWritableNoResponse.add(characteristic);
        });
    }

    public boolean isExistInNotification(BluetoothGattCharacteristic characteristic) {
        return uuidsForNotification.contains(characteristic.getUuid());
    }

    public boolean isExistInWritable(BluetoothGattCharacteristic characteristic) {
        return uuidsForWritable.contains(characteristic.getUuid()) || uuidsForWritableNoResponse.contains(characteristic.getUuid());
    }
}
