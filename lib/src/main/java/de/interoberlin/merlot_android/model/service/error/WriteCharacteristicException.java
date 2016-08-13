package de.interoberlin.merlot_android.model.service.error;

import android.bluetooth.BluetoothGattCharacteristic;

import de.interoberlin.merlot_android.model.service.BluetoothGattStatus;

public class WriteCharacteristicException extends Exception {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public final BluetoothGattCharacteristic characteristic;
    public final int status;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public WriteCharacteristicException(BluetoothGattCharacteristic characteristic, int status) {
        this.characteristic = characteristic;
        this.status = status;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    @Override
    public String getMessage() {
        return toString();
    }

    @Override
    public String toString() {
        return BluetoothGattStatus.toString(status);
    }

    // </editor-fold>
}
