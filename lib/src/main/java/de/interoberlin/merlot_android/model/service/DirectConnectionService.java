package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

import com.google.gson.Gson;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.config.ECharacteristic;
import de.interoberlin.merlot_android.model.config.EDescriptor;
import de.interoberlin.merlot_android.model.config.EService;
import de.interoberlin.merlot_android.model.parser.BleDataParser;
import de.interoberlin.merlot_android.model.parser.DataPackage;
import de.interoberlin.merlot_android.model.service.error.CharacteristicNotFoundException;
import de.interoberlin.merlot_android.model.util.BleUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static rx.Observable.error;

/**
 * A class representing the Direct Connection BLE Service.
 * The functionality and characteristics available when a device is in DIRECT_CONNECTION mode.
 */
public class DirectConnectionService extends BaseService {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = DirectConnectionService.class.getSimpleName();

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public DirectConnectionService(BleDevice device, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        super(device, gatt, receiver);
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static Observable<DirectConnectionService> connect(final Context context, final BleDevice bleDevice,
                                                              final BluetoothDevice device) {
        final BluetoothGattReceiver receiver = new BluetoothGattReceiver();
        return doConnect(context, device, receiver, false)
                .flatMap(new BondingReceiver.BondingFunc1(context))
                .map(new Func1<BluetoothGatt, DirectConnectionService>() {
                    @Override
                    public DirectConnectionService call(BluetoothGatt gatt) {
                        bleDevice.setGatt(gatt);
                        return new DirectConnectionService(bleDevice, gatt, receiver);
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
                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
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

    public Observable<Reading> subscribe(final ECharacteristic characteristic) {
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(mBluetoothGatt.getServices(), EService.DIRECT_CONNECTION.getId(), characteristic.getId());

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
                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
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

    public Observable<BluetoothGattCharacteristic> stopSubscribing() {
        ECharacteristic c = ECharacteristic.DATA;
        EService s = EService.DIRECT_CONNECTION;

        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), s.getId(), c.getId());
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(c.getId()));
        }
        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                characteristic, EDescriptor.DATA_NOTIFICATIONS.getId());
        return mBluetoothGattReceiver
                .unsubscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor);
    }

    // </editor-fold>
}
