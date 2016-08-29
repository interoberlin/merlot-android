package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.google.gson.Gson;

import de.interoberlin.mate.lib.model.Log;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.parser.BleDataParser;
import de.interoberlin.merlot_android.model.parser.DataPackage;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EDescriptor;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.merlot_android.model.service.error.CharacteristicNotFoundException;
import de.interoberlin.merlot_android.model.util.BleUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static rx.Observable.error;

/**
 * A class representing the basic characteristics of the BLE service a Device should have
 */
public class BaseService extends Service {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BaseService.class.getSimpleName();

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

    public static Observable<BaseService> connect(final Context context, final BleDevice bleDevice,
                                                              final BluetoothDevice device) {
        final BluetoothGattReceiver receiver = new BluetoothGattReceiver();
        return doConnect(context, device, receiver, false)
                .flatMap(new BondingReceiver.BondingFunc1(context))
                .map(new Func1<BluetoothGatt, BaseService>() {
                    @Override
                    public BaseService call(BluetoothGatt gatt) {
                        bleDevice.setGatt(gatt);
                        return new BaseService(bleDevice, gatt, receiver);
                    }
                });
    }

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

    /**
     * Reads a single value from a characteristic
     *
     * @param characteristic characteristic
     * @return observable containing reading
     */
    public Observable<Reading> read(final ECharacteristic characteristic) {
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(mBluetoothGatt.getServices(), characteristic.getService().getId(), characteristic.getId());

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristic.getId()));
        }

        return mBluetoothGattReceiver
                .readCharacteristic(mBluetoothGatt, c)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic c) {
                        Log.d(TAG, "Received " + new String(c.getValue()));

                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        Log.d(TAG, "Processed " + s);
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                for (DataPackage.Data dataPoint : data.readings) {
                                    subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                            dataPoint.meaning, dataPoint.path, dataPoint.value));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<Reading> subscribe(final EService service, final ECharacteristic characteristic) {
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(mBluetoothGatt.getServices(), service.getId(), characteristic.getId());

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristic.getId()));
        }

        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                c, EDescriptor.DATA_NOTIFICATIONS.getId());
        return mBluetoothGattReceiver
                .subscribeToCharacteristicChanges(mBluetoothGatt, c, descriptor)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic c) {
                        Log.d(TAG, "Received " + new String(c.getValue()));

                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        Log.d(TAG, "Processed " + s);

                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                for (DataPackage.Data dataPoint : data.readings) {
                                    subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                            dataPoint.meaning, dataPoint.path, dataPoint.value));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<BluetoothGattCharacteristic> stopSubscribing(final EService service, final ECharacteristic characteristic) {

        BluetoothGattCharacteristic gattCharacteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), service.getId(), characteristic.getId());
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristic.getId()));
        }
        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                gattCharacteristic, EDescriptor.DATA_NOTIFICATIONS.getId());
        return mBluetoothGattReceiver
                .unsubscribeToCharacteristicChanges(mBluetoothGatt, gattCharacteristic, descriptor);
    }

    // </editor-fold>
}