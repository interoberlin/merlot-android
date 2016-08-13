package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.interoberlin.merlot_android.model.service.error.CharacteristicNotFoundException;
import de.interoberlin.merlot_android.model.util.BleUtils;
import de.interoberlin.merlot_android.model.util.DeviceCompatibilityUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_SFLOAT;
import static android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16;
import static rx.Observable.error;
import static rx.Observable.just;

public class Service {
    // <editor-fold defaultstate="collapsed" desc="Members">

    protected BluetoothGatt mBluetoothGatt;
    protected final BluetoothGattReceiver mBluetoothGattReceiver;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    protected Service(BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        mBluetoothGatt = gatt;
        mBluetoothGattReceiver = receiver;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public BluetoothGatt getGatt() {
        return mBluetoothGatt;
    }

    protected static Observable<? extends BluetoothGatt> doConnect(final Context context,
            final BluetoothDevice bluetoothDevice, final BluetoothGattReceiver receiver,
            final boolean unBond) {
        return receiver
                .connect(context, bluetoothDevice)
                .flatMap(new Func1<BluetoothGatt, Observable<? extends BluetoothGatt>>() {
                    @Override
                    public Observable<? extends BluetoothGatt> call(BluetoothGatt gatt) {
                        if (unBond && gatt.getDevice().getBondState() != BOND_NONE) {
                            // It was previously bonded on direct connection and needs to remove
                            // bond and update the services to work properly
                            DeviceCompatibilityUtils.removeBond(gatt.getDevice());
                            return receiver.connect(context, bluetoothDevice)
                                    .flatMap(new Func1<BluetoothGatt, Observable<? extends BluetoothGatt>>() {
                                        @Override
                                        public Observable<? extends BluetoothGatt> call(BluetoothGatt gatt) {
                                            DeviceCompatibilityUtils.refresh(gatt);
                                            return receiver.discoverServices(gatt);
                                        }
                                    });
                        } else if (!unBond && gatt.getDevice().getBondState() == BOND_NONE) {
                            // It was previously connected to master module and has not updated the services.
                            DeviceCompatibilityUtils.refresh(gatt);
                            return doConnect(context, bluetoothDevice, receiver, true);
                        }
                        return receiver.discoverServices(gatt);
                    }
                });
    }

    public Observable<BluetoothGattCharacteristic> write(byte[] bytes,
                                                         String serviceUuid,
                                                         String characteristicUuid) {
        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), serviceUuid, characteristicUuid);
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(characteristicUuid));
        }
        characteristic.setValue(bytes);
        return mBluetoothGattReceiver.writeCharacteristic(mBluetoothGatt, characteristic);
    }

    protected Observable<BluetoothGatt> longWrite(final byte[] data, String serviceUuid,
                                                  final String characteristicUuid) {

        final BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), serviceUuid, characteristicUuid);

        if (characteristic == null)
            return error(new CharacteristicNotFoundException(characteristicUuid));

        final LongWriteDataParser dataParser = new LongWriteDataParser(data);
        return Observable
                .create(new Observable.OnSubscribe<BluetoothGatt>() {
                    @Override
                    public void call(Subscriber<? super BluetoothGatt> subscriber) {
                        mBluetoothGatt.beginReliableWrite();
                        sendPayload(dataParser, characteristic, subscriber);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .timeout(20, TimeUnit.SECONDS)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        DeviceCompatibilityUtils.refresh(mBluetoothGatt);
                    }
                });
    }

    private void sendPayload(final LongWriteDataParser parser,
                             final BluetoothGattCharacteristic characteristic,
                             final Subscriber<? super BluetoothGatt> subscriber) {
        final byte[] data = parser.getData();

        if (data.length == 0) {
            mBluetoothGatt.executeReliableWrite();
            return;
        }

        characteristic.setValue(data);
        mBluetoothGattReceiver.reliableWriteCharacteristic(mBluetoothGatt, characteristic, subscriber);

        Observable.create(
                new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> s) {
                        sendPayload(parser, characteristic, subscriber);
                    }
                })
                .delaySubscription(400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public Observable<BluetoothGattCharacteristic> readCharacteristic(String serviceUuid,
                                                                      String characteristicUuid,
                                                                      final String what) {
        BluetoothGattCharacteristic characteristic = BleUtils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), serviceUuid, characteristicUuid);
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(what));
        }
        return mBluetoothGattReceiver.readCharacteristic(mBluetoothGatt, characteristic);
    }

    protected Observable<Float> readFloatCharacteristic(String serviceUuid,
                                                        String characteristicUuid,
                                                        final String what) {
        return readCharacteristic(serviceUuid, characteristicUuid, what)
                .flatMap(new Func1<BluetoothGattCharacteristic, Observable<Float>>() {
                    @Override
                    public Observable<Float> call(BluetoothGattCharacteristic charac) {
                        if (charac.getValue() == null || charac.getValue().length == 0) {
                            error(new CharacteristicNotFoundException(what));
                        }
                        return just(charac.getFloatValue(FORMAT_SFLOAT, 0));
                    }
                });
    }

    protected Observable<Integer> readIntegerCharacteristic(String serviceUuid,
                                                            String characteristicUuid,
                                                            final String what) {
        return readCharacteristic(serviceUuid, characteristicUuid, what)
                .flatMap(new Func1<BluetoothGattCharacteristic, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(BluetoothGattCharacteristic charac) {
                        if (charac.getValue() == null || charac.getValue().length == 0) {
                            error(new CharacteristicNotFoundException(what));
                        }
                        return just(charac.getIntValue(FORMAT_UINT16, 0));
                    }
                });
    }

    protected Observable<Integer> readByteAsAnIntegerCharacteristic(String serviceUuid,
                                                                    String characteristicUuid,
                                                                    final String what) {
        return readCharacteristic(serviceUuid, characteristicUuid, what)
                .flatMap(new Func1<BluetoothGattCharacteristic, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(BluetoothGattCharacteristic charac) {
                        if (charac.getValue() == null || charac.getValue().length == 0) {
                            error(new CharacteristicNotFoundException(what));
                        }
                        return just((int) charac.getValue()[0]);
                    }
                });
    }


    protected Observable<String> readStringCharacteristic(String serviceUuid,
                                                          String characteristicUuid,
                                                          final String what) {
        return readCharacteristic(serviceUuid, characteristicUuid, what)
                .flatMap(new Func1<BluetoothGattCharacteristic, Observable<String>>() {
                    @Override
                    public Observable<String> call(BluetoothGattCharacteristic charac) {
                        String value = charac.getStringValue(0);
                        if (value == null) {
                            return error(new CharacteristicNotFoundException(what));
                        }
                        return just(value);
                    }
                });
    }

    protected Observable<UUID> readUuidCharacteristic(String service, String characteristic,
                                                      final String what) {
        return readCharacteristic(service, characteristic, what)
                .flatMap(new Func1<BluetoothGattCharacteristic, Observable<UUID>>() {
                    @Override
                    public Observable<UUID> call(BluetoothGattCharacteristic characteristic) {
                        byte[] value = characteristic.getValue();
                        if (value == null) {
                            return error(new CharacteristicNotFoundException(what));
                        }
                        return just(BleUtils.fromBytes(value));
                    }
                });
    }

    // </editor-fold>
}