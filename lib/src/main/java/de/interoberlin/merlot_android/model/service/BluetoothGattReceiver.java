package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.interoberlin.merlot_android.model.service.error.DisconnectionException;
import de.interoberlin.merlot_android.model.service.error.GattException;
import de.interoberlin.merlot_android.model.service.error.WriteCharacteristicException;
import de.interoberlin.merlot_android.model.util.DeviceCompatibilityUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION;
import static android.bluetooth.BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.bluetooth.BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class BluetoothGattReceiver extends BluetoothGattCallback {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BluetoothGattReceiver.class.getSimpleName();

    private Context context;

    private volatile Subscriber<? super BluetoothGatt> connectionChangesSubscriber;
    private volatile Subscriber<? super BluetoothGatt> disconnectedSubscriber;
    private volatile Subscriber<? super BluetoothGatt> bluetoothGattServiceSubscriber;
    private volatile Subscriber<? super BluetoothGattCharacteristic> valueChangesSubscriber;
    private volatile Subscriber<? super BluetoothGattCharacteristic> valueChangesUnSubscriber;
    private volatile Map<UUID, Subscriber<? super BluetoothGattCharacteristic>>
            writeCharacteristicsSubscriberMap = new ConcurrentHashMap<>();
    private volatile Map<UUID, Subscriber<? super BluetoothGattCharacteristic>>
            readCharacteristicsSubscriberMap = new ConcurrentHashMap<>();
    private volatile Subscriber<? super BluetoothGatt> reliableWriteSubscriber;

    public Observable<BluetoothGatt> connect(final Context context, final BluetoothDevice bluetoothDevice) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                connectionChangesSubscriber = subscriber;
                bluetoothDevice.connectGatt(context, false, BluetoothGattReceiver.this);
            }
        });
    }

    static class UndocumentedBleStuff {

        static boolean isUndocumentedErrorStatus(int status) {
            return status == 133 || status == 137;
        }

        static void fixUndocumentedBleStatusProblem(final Context context, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
            Log.d(TAG, "Fix undocumented BLE status problem");
            DeviceCompatibilityUtils.refresh(gatt);
            gatt.getDevice().connectGatt(context, false, receiver);
        }
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public Observable<BluetoothGatt> discoverServices(final BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "Discover services");
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                bluetoothGattServiceSubscriber = subscriber;
                //if (gatt.getServices() != null && gatt.getServices().size() > 0)
                //    bluetoothGattServiceSubscriber.onNext(gatt);
                //else // TODO: we don't cache bc we don't know if the services are up to date...
                bluetoothGatt.discoverServices();
            }
        });
    }

    public Observable<BluetoothGatt> disconnect(final BluetoothGatt bluetoothGatt) {
        Log.d(TAG, "Disconnect");
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                disconnectedSubscriber = subscriber;
                bluetoothGatt.disconnect();
            }
        });
    }

    public Observable<BluetoothGattCharacteristic>
    writeCharacteristic(final BluetoothGatt bluetoothGatt,
                        final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Write characteristic");
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                writeCharacteristicsSubscriberMap.put(characteristic.getUuid(), subscriber);
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        });
    }

    public void reliableWriteCharacteristic(final BluetoothGatt bluetoothGatt,
                                            final BluetoothGattCharacteristic characteristic,
                                            Subscriber<? super BluetoothGatt> subscriber) {
        Log.d(TAG, "Reliable write characteristic");
        if (reliableWriteSubscriber == null) reliableWriteSubscriber = subscriber;
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    public Observable<BluetoothGattCharacteristic> readCharacteristic(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "Read characteristic");
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                readCharacteristicsSubscriberMap.put(characteristic.getUuid(), subscriber);
                gatt.readCharacteristic(characteristic);
            }
        });
    }

    public Observable<BluetoothGattCharacteristic> subscribeToCharacteristicChanges(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic,
            final BluetoothGattDescriptor descriptor) {
        Log.d(TAG, "Subscribe to characteristic changes");
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                valueChangesSubscriber = subscriber;
                gatt.setCharacteristicNotification(characteristic, true);
                descriptor.setValue(ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        });
    }

    public Observable<BluetoothGattCharacteristic> unsubscribeToCharacteristicChanges(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic,
            final BluetoothGattDescriptor descriptor) {
        Log.d(TAG, "Unsubscribe to characteristic changes");
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                valueChangesUnSubscriber = subscriber;
                gatt.setCharacteristicNotification(characteristic, false);
                descriptor.setValue(DISABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        });
    }

    // </editor-fold>

    // --------------------
    // Methods - Callbacks
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callbacks">

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//        if (isConnectionError(status)) {
//            tryToReconnect(gatt, this); return;
//        } else if (isGattError(status)) {
//            fixGattError(gatt, this);
//            connectionChangesSubscriber.onError(new DisconnectionException(status + ""));
//            return;
//        }

        if (UndocumentedBleStuff.isUndocumentedErrorStatus(status)) {
            UndocumentedBleStuff.fixUndocumentedBleStatusProblem(context, gatt, this);
            return;
        }
        if (status != GATT_SUCCESS) return;

        if (newState == STATE_CONNECTED) { // on connected
            if (connectionChangesSubscriber != null) connectionChangesSubscriber.onNext(gatt);
        } else if (newState == STATE_DISCONNECTED) {
            if (disconnectedSubscriber != null) { // disconnected voluntarily
                gatt.close(); // should stay here since you might want to reconnect if involuntarily
                disconnectedSubscriber.onNext(gatt);
                disconnectedSubscriber.onCompleted();
            } else { // disconnected involuntarily because an error occurred
                if (connectionChangesSubscriber != null)
                    connectionChangesSubscriber.onError(new DisconnectionException(status + ""));
            }
        } /*else if (BluetoothGattStatus.isFailureStatus(status)) {
            if (connectionChangesSubscriber != null)  // TODO: unreachable -propagate error earlier
                connectionChangesSubscriber.onError(new GattException(status + ""));
        }*/
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (bluetoothGattServiceSubscriber == null) return;
        bluetoothGattServiceSubscriber.onNext(gatt);
    }

    @Override
    public void onReliableWriteCompleted(final BluetoothGatt gatt, int status) {
        if (reliableWriteSubscriber == null) return;

        if (status == GATT_SUCCESS) {
            reliableWriteSubscriber.onNext(gatt);
            reliableWriteSubscriber.onCompleted();
        } else if (GATT_INSUFFICIENT_AUTHENTICATION == status || GATT_INSUFFICIENT_ENCRYPTION == status) {
            reliableWriteSubscriber.onError(new GattException("Authentication"));
//        } else if (isGattError(status)) {
//            fixGattError(gatt, this);
//            reliableWriteSubscriber.onError(new UndocumentedException());
        } else {
            reliableWriteSubscriber.onError(new GattException("Reliable write failed."));
        }

        reliableWriteSubscriber = null;
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      final BluetoothGattCharacteristic characteristic,
                                      int status) {
        Subscriber<? super BluetoothGattCharacteristic> subscriber =
                writeCharacteristicsSubscriberMap.remove(characteristic.getUuid());
        if (status == GATT_SUCCESS) {
            subscriber.onNext(characteristic);
        } else if (GATT_INSUFFICIENT_AUTHENTICATION == status || GATT_INSUFFICIENT_ENCRYPTION == status) {
            Observable.just(gatt)
                    .flatMap(new BondingReceiver.BondingFunc1(context))
                    .map(new Func1<BluetoothGatt, Boolean>() {
                        @Override
                        public Boolean call(BluetoothGatt bluetoothGatt) {
                            return bluetoothGatt.writeCharacteristic(characteristic);
                        }
                    })
                    .subscribe();
        } else if (UndocumentedBleStuff.isUndocumentedErrorStatus(status)) {
            UndocumentedBleStuff.fixUndocumentedBleStatusProblem(context, gatt, this);
        } else {
            subscriber.onError(new WriteCharacteristicException(characteristic, status));
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic characteristic,
                                     int status) {
        Subscriber<? super BluetoothGattCharacteristic> subscriber =
                readCharacteristicsSubscriberMap.remove(characteristic.getUuid());
        if (status == GATT_SUCCESS) {
            subscriber.onNext(characteristic);
        } else if (GATT_INSUFFICIENT_AUTHENTICATION == status || GATT_INSUFFICIENT_ENCRYPTION == status) {
            Observable.just(gatt)
                    .flatMap(new BondingReceiver.BondingFunc1(context))
                    .map(new Func1<BluetoothGatt, Boolean>() {
                        @Override
                        public Boolean call(BluetoothGatt bluetoothGatt) {
                            return bluetoothGatt.readCharacteristic(characteristic);
                        }
                    })
                    .subscribe();
        } else if (UndocumentedBleStuff.isUndocumentedErrorStatus(status)) {
            UndocumentedBleStuff.fixUndocumentedBleStatusProblem(context, gatt, this);
        } else {
            subscriber.onError(new WriteCharacteristicException(characteristic, status));
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if (valueChangesUnSubscriber != null) {
            valueChangesUnSubscriber.onNext(descriptor.getCharacteristic());
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        valueChangesSubscriber.onNext(characteristic);
    }

    // </editor-fold>
}
