package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;

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

    private Context context;

    private volatile Subscriber<? super BluetoothGatt> mConnectionChangesSubscriber;
    private volatile Subscriber<? super BluetoothGatt> mDisconnectedSubscriber;
    private volatile Subscriber<? super BluetoothGatt> mBluetoothGattServiceSubscriber;
    private volatile Subscriber<? super BluetoothGattCharacteristic> mValueChangesSubscriber;
    private volatile Subscriber<? super BluetoothGattCharacteristic> mValueChangesUnSubscriber;
    private volatile Map<UUID, Subscriber<? super BluetoothGattCharacteristic>>
            mWriteCharacteristicsSubscriberMap = new ConcurrentHashMap<>();
    private volatile Map<UUID, Subscriber<? super BluetoothGattCharacteristic>>
            mReadCharacteristicsSubscriberMap = new ConcurrentHashMap<>();
    private volatile Subscriber<? super BluetoothGatt> mReliableWriteSubscriber;

    public Observable<BluetoothGatt> connect(final Context context, final BluetoothDevice bluetoothDevice) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                mConnectionChangesSubscriber = subscriber;
                bluetoothDevice.connectGatt(context, false, BluetoothGattReceiver.this);
            }
        });
    }

    static class UndocumentedBleStuff {

        static boolean isUndocumentedErrorStatus(int status) {
            return status == 133 || status == 137;
        }

        static void fixUndocumentedBleStatusProblem(final Context context, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
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
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                mBluetoothGattServiceSubscriber = subscriber;
                //if (bluetoothGatt.getServices() != null && bluetoothGatt.getServices().size() > 0)
                //    mBluetoothGattServiceSubscriber.onNext(bluetoothGatt);
                //else // TODO: we don't cache bc we don't know if the services are up to date...
                bluetoothGatt.discoverServices();
            }
        });
    }

    public Observable<BluetoothGatt> disconnect(final BluetoothGatt bluetoothGatt) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(Subscriber<? super BluetoothGatt> subscriber) {
                mDisconnectedSubscriber = subscriber;
                bluetoothGatt.disconnect();
            }
        });
    }

    public Observable<BluetoothGattCharacteristic>
    writeCharacteristic(final BluetoothGatt bluetoothGatt,
                        final BluetoothGattCharacteristic characteristic) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                mWriteCharacteristicsSubscriberMap.put(characteristic.getUuid(), subscriber);
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        });
    }

    public void reliableWriteCharacteristic(final BluetoothGatt bluetoothGatt,
                                            final BluetoothGattCharacteristic characteristic,
                                            Subscriber<? super BluetoothGatt> subscriber) {
        if (mReliableWriteSubscriber == null) mReliableWriteSubscriber = subscriber;
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    public Observable<BluetoothGattCharacteristic> readCharacteristic(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                mReadCharacteristicsSubscriberMap.put(characteristic.getUuid(), subscriber);
                gatt.readCharacteristic(characteristic);
            }
        });
    }

    public Observable<BluetoothGattCharacteristic> subscribeToCharacteristicChanges(
            final BluetoothGatt gatt,
            final BluetoothGattCharacteristic characteristic,
            final BluetoothGattDescriptor descriptor) {
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                mValueChangesSubscriber = subscriber;
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
        return Observable.create(new Observable.OnSubscribe<BluetoothGattCharacteristic>() {
            @Override
            public void call(Subscriber<? super BluetoothGattCharacteristic> subscriber) {
                mValueChangesUnSubscriber = subscriber;
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
//            mConnectionChangesSubscriber.onError(new DisconnectionException(status + ""));
//            return;
//        }

        if (UndocumentedBleStuff.isUndocumentedErrorStatus(status)) {
            UndocumentedBleStuff.fixUndocumentedBleStatusProblem(context, gatt, this);
            return;
        }
        if (status != GATT_SUCCESS) return;

        if (newState == STATE_CONNECTED) { // on connected
            if (mConnectionChangesSubscriber != null) mConnectionChangesSubscriber.onNext(gatt);
        } else if (newState == STATE_DISCONNECTED) {
            if (mDisconnectedSubscriber != null) { // disconnected voluntarily
                gatt.close(); // should stay here since you might want to reconnect if involuntarily
                mDisconnectedSubscriber.onNext(gatt);
                mDisconnectedSubscriber.onCompleted();
            } else { // disconnected involuntarily because an error occurred
                if (mConnectionChangesSubscriber != null)
                    mConnectionChangesSubscriber.onError(new DisconnectionException(status + ""));
            }
        } /*else if (BluetoothGattStatus.isFailureStatus(status)) {
            if (mConnectionChangesSubscriber != null)  // TODO: unreachable -propagate error earlier
                mConnectionChangesSubscriber.onError(new GattException(status + ""));
        }*/
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (mBluetoothGattServiceSubscriber == null) return;
        mBluetoothGattServiceSubscriber.onNext(gatt);
    }

    @Override
    public void onReliableWriteCompleted(final BluetoothGatt gatt, int status) {
        if (mReliableWriteSubscriber == null) return;

        if (status == GATT_SUCCESS) {
            mReliableWriteSubscriber.onNext(gatt);
            mReliableWriteSubscriber.onCompleted();
        } else if (GATT_INSUFFICIENT_AUTHENTICATION == status || GATT_INSUFFICIENT_ENCRYPTION == status) {
            mReliableWriteSubscriber.onError(new GattException("Authentication"));
//        } else if (isGattError(status)) {
//            fixGattError(gatt, this);
//            mReliableWriteSubscriber.onError(new UndocumentedException());
        } else {
            mReliableWriteSubscriber.onError(new GattException("Reliable write failed."));
        }

        mReliableWriteSubscriber = null;
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      final BluetoothGattCharacteristic characteristic,
                                      int status) {
        Subscriber<? super BluetoothGattCharacteristic> subscriber =
                mWriteCharacteristicsSubscriberMap.remove(characteristic.getUuid());
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
                mReadCharacteristicsSubscriberMap.remove(characteristic.getUuid());
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
        if (mValueChangesUnSubscriber != null) {
            mValueChangesUnSubscriber.onNext(descriptor.getCharacteristic());
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        mValueChangesSubscriber.onNext(characteristic);
    }

    // </editor-fold>
}
