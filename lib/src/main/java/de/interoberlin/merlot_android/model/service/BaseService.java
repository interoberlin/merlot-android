package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
        android.util.Log.v(TAG, "Connect " + bleDevice.getName());
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
        android.util.Log.v(TAG, "Disconnect");
        return bluetoothGattReceiver
                .disconnect(gatt)
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
        android.util.Log.v(TAG, "Read " + characteristic.getName());
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(gatt.getServices(), characteristic.getService().getId(), characteristic.getId());

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristic.getId()));
        }

        return bluetoothGattReceiver
                .readCharacteristic(gatt, c)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic c) {
                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        Log.v(TAG, "Received " + s);
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                try {
                                    DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                    for (DataPackage.Data dataPoint : data.readings) {
                                        subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                                dataPoint.meaning, dataPoint.path, dataPoint.value));
                                    }
                                } catch (JsonSyntaxException e) {
                                    subscriber.onNext(new Reading(System.currentTimeMillis(), System.currentTimeMillis(), "", "", s));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<Reading> subscribe(final EService service, final ECharacteristic characteristic) {
        Log.v(TAG, "Subscribe " + service.getName() + " / " + characteristic.getName());
        BluetoothGattCharacteristic c = BleUtils.getCharacteristicInServices(gatt.getServices(), service.getId(), characteristic.getId());

        if (characteristic == null) {
            return error(new CharacteristicNotFoundException("Service " + service.getId() + " / characteristic " + characteristic.getId()));
        }

        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                c, EDescriptor.DATA_NOTIFICATIONS.getId());
        return bluetoothGattReceiver
                .subscribeToCharacteristicChanges(gatt, c, descriptor)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic c) {
                        return BleDataParser.getFormattedValue(device.getType(), characteristic, c.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        Log.v(TAG, "Received " + s);
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                try {
                                    DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                    for (DataPackage.Data dataPoint : data.readings) {
                                        subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                                dataPoint.meaning, dataPoint.path, dataPoint.value));
                                    }
                                } catch (JsonSyntaxException e) {
                                    subscriber.onNext(new Reading(System.currentTimeMillis(), System.currentTimeMillis(), "", "", s));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<BluetoothGattCharacteristic> stopSubscribing(final EService service, final ECharacteristic characteristic) {
        Log.v(TAG, "Stop subscribing " + service.getName() + " / " + characteristic.getName());
        BluetoothGattCharacteristic gattCharacteristic = BleUtils.getCharacteristicInServices(
                gatt.getServices(), service.getId(), characteristic.getId());
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristic.getId()));
        }
        BluetoothGattDescriptor descriptor = BleUtils.getDescriptorInCharacteristic(
                gattCharacteristic, EDescriptor.DATA_NOTIFICATIONS.getId());
        return bluetoothGattReceiver
                .unsubscribeToCharacteristicChanges(gatt, gattCharacteristic, descriptor);
    }

    // </editor-fold>
}