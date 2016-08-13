package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothGatt;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import rx.Observable;
import rx.functions.Func1;

/**
 * A class representing the basic characteristics of the BLE service a Device should have
 */
public class BaseService extends Service {
    // <editor-fold defaultstate="collapsed" desc="Members">

    protected final BleDevice device;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    protected BaseService(BleDevice device, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        super(gatt, receiver);
        this.device = device;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Disconnects and closes the gatt. It should not be called directly use
     * {@link BleDevice#disconnect()} instead.
     *
     * @return an observable of the device that was connected.
     */
    public Observable<BleDevice> disconnect() {
        return mBluetoothGattReceiver
                .disconnect(mBluetoothGatt)
                .map(new Func1<BluetoothGatt, BleDevice>() {
                    @Override
                    public BleDevice call(BluetoothGatt gatt) {
                        return device;
                    }
                });
    }

    // </editor-fold>
}