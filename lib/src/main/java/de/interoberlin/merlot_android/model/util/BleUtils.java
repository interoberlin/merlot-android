package de.interoberlin.merlot_android.model.util;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class BleUtils {
    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static UUID fromBytes(byte[] value) {
        ByteBuffer bb = ByteBuffer.wrap(value);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    public static BluetoothGattService getServiceForUuid(List<BluetoothGattService> services,
                                                         String serviceUuid) {
        for (BluetoothGattService service : services) {
            if (service.getUuid().toString().equals(serviceUuid)) return service;
        }
        return null;
    }

    public static BluetoothGattCharacteristic getCharacteristicForUuid(
            List<BluetoothGattCharacteristic> characteristics,
            String characteristicUuid) {
        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristic.getUuid().toString().equals(characteristicUuid)) return characteristic;
        }
        return null;
    }

    public static BluetoothGattCharacteristic getCharacteristicInServices(
            List<BluetoothGattService> services,
            String serviceUuid,
            String characteristicUuid) {
        BluetoothGattService service = getServiceForUuid(services, serviceUuid);
        if (service == null) return null;

        return getCharacteristicForUuid(service.getCharacteristics(), characteristicUuid);
    }

    public static BluetoothGattDescriptor getDescriptorInCharacteristic(
            BluetoothGattCharacteristic characteristic,
            String descriptorUuid) {
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptors) {
            if (descriptor.getUuid().toString().equals(descriptorUuid)) return descriptor;
        }
        return null;
    }

    /*
    public static String getCharacteristicInServicesAsString(List<BluetoothGattService> services,
                                                             String serviceUuid,
                                                             String characteristicUuid) {

        BluetoothGattCharacteristic characteristic = getCharacteristicInServices(
                services, serviceUuid, characteristicUuid);
        if (characteristic == null) return "";
        return characteristic.getStringValue(0);
    }
    */

    // </editor-fold>
}
