package de.interoberlin.merlot_android.model.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.google.common.collect.EvictingQueue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import de.interoberlin.merlot_android.MerlotRealm;
import de.interoberlin.merlot_android.R;
import de.interoberlin.merlot_android.controller.MappingController;
import de.interoberlin.merlot_android.model.IDisplayable;
import de.interoberlin.merlot_android.model.parser.BleDataParser;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EDevice;
import de.interoberlin.merlot_android.model.repository.EService;
import de.interoberlin.merlot_android.model.service.BaseService;
import de.interoberlin.merlot_android.model.service.BleDeviceManager;
import de.interoberlin.merlot_android.model.service.Reading;
import de.interoberlin.merlot_android.model.util.BleUtils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

/**
 * A class representing a BLE Device
 */
public class BleDevice extends RealmObject implements IDisplayable {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BleDevice.class.getSimpleName();

    public static final int READING_HISTORY = 50;

    private String name;
    @PrimaryKey private String address;
    @Ignore  private EDevice type;
    private String typeName;
    @Ignore int rssi;
    @Ignore private BleDeviceManager deviceManager;
    @Ignore private Observable<? extends BaseService> serviceObservable;
    @Ignore private BluetoothGatt gatt;

    @Ignore private List<BluetoothGattService> services;
    @Ignore private List<BluetoothGattCharacteristic> characteristics;

    @Ignore private Map<String, Queue<Reading>> readings;
    @Ignore private Map<String, Reading> latestReadings;
    @Ignore private Map<ECharacteristic, Subscription> subscriptions;

    @Ignore private boolean connected;
    @Ignore private boolean reading;
    @Ignore private boolean subscribing;
    private boolean autoConnectEnabled;

    @Ignore private ConnectableObservable<Reading> readingObservable;

    @Ignore private OnChangeListener ocListener;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public BleDevice() {
        super();
    }

    public BleDevice(Context context, BluetoothDevice device, int rssi, BleDeviceManager manager) {
        super();

        this.name = device.getName();
        this.address = device.getAddress();
        this.type = EDevice.fromString(device.getName());
        this.typeName = (type != null) ? type.getName() : EDevice.UNKNOWN.toString();
        this.rssi = rssi;
        this.deviceManager = manager;
        this.serviceObservable = BaseService.connect(context, this, device).cache();

        this.services = new ArrayList<>();
        this.characteristics = new ArrayList<>();
        this.readings = new HashMap<>();
        this.latestReadings = new HashMap<>();
        this.subscriptions = new HashMap<>();

        this.connected = false;
        this.reading = false;
        this.subscribing = false;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Initializes a ble device object
     *
     * @param context context
     */
    public void init(Context context) {
        // Read device from realm
        final Realm realm = new MerlotRealm(context).getRealm();
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Log.d(TAG, "Realm count " + bgRealm.where(BleDevice.class).count());
                BleDevice realmResult = bgRealm.where(BleDevice.class)
                        .equalTo("address", address)
                        .findFirst();

                if (realmResult != null) {
                    Log.d(TAG, "Realm entry name " + realmResult.getName() + " / address " + realmResult.getAddress() + " / type " + realmResult.getTypeName() + " / autoConnect " + autoConnectEnabled);

                    setAutoConnectEnabled(realmResult.isAutoConnectEnabled());
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

            }
        });
    }

    /**
     * Connects the device
     *
     * @return observable containing a base service
     */
    public Observable<? extends BaseService> connect() {
        Log.d(TAG, "Connect");

        setConnected(true);
        return serviceObservable;
    }

    /**
     * Disconnects the device
     *
     * @return observable containing the device
     */
    public Observable<BleDevice> disconnect() {
        Log.d(TAG, "Disconnect");

        setConnected(false);
        deviceManager.removeDevice(this);
        return serviceObservable
                .flatMap(new Func1<BaseService, Observable<BleDevice>>() {
                    @Override
                    public Observable<BleDevice> call(BaseService service) {
                        return service.disconnect();
                    }
                });
    }

    /**
     * Disconnects gatt connection and closes it
     */
    public void close() {
        Log.d(TAG, "Close");

        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
        }
    }

    /**
     * Reads a single value from a characteristic
     *
     * @param characteristic characteristic
     */
    public void read(final ECharacteristic characteristic) {
        read(characteristic, false);
    }

    /**
     * Reads a single value from a characteristic
     *
     * @param characteristic characteristic
     * @param debug          debug
     */
    public void read(final ECharacteristic characteristic, final boolean debug) {
        Log.d(TAG, "Read " + characteristic.getId());

        connect()
                .flatMap(new Func1<BaseService, Observable<BluetoothGattCharacteristic>>() {
                    @Override
                    public Observable<BluetoothGattCharacteristic> call(BaseService baseService) {
                        return baseService.readCharacteristic(characteristic.getService().getId(), characteristic.getId(), "");
                    }
                }).subscribe(new Observer<BluetoothGattCharacteristic>() {
            @Override
            public void onCompleted() {
                reading = false;
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onNext(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
                disconnect();

                String value = BleDataParser.getFormattedValue(null, characteristic, bluetoothGattCharacteristic.getValue());
                Log.d(TAG, "Read " + characteristic.getId() + " : " + value);
                setCharacteristicValue(characteristic.getId(), value, debug);
            }
        });
    }

    /**
     * Subscribes to a characteristic with a given {@code id}
     *
     * @param context        context
     * @param characteristic characteristic
     * @return subscription
     */
    public Subscription subscribe(final Context context, final ECharacteristic characteristic) {
        return subscribe(context, characteristic, false);
    }

    /**
     * Subscribes to a characteristic with a given {@code id}
     *
     * @param context        context
     * @param characteristic characteristic
     * @param debug          debug version
     * @return subscription
     */
    public Subscription subscribe(final Context context, final ECharacteristic characteristic, final boolean debug) {
        Log.d(TAG, "Subscribe " + characteristic.getId());

        Subscription subscription = connect()
                .flatMap(new Func1<BaseService, ConnectableObservable<Reading>>() {
                    @Override
                    public ConnectableObservable<Reading> call(BaseService baseService) {
                        ConnectableObservable<Reading> readingObservable = baseService.subscribe(characteristic.getService(), characteristic).publish();
                        setReadingObservable(readingObservable);
                        MappingController.getInstance(context).flangeAll();

                        return readingObservable;
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        disconnect();
                    }
                }).subscribe(new Observer<Reading>() {
                    @Override
                    public void onCompleted() {
                        reading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Reading reading) {
                        Log.d(TAG, "Next value " + reading.meaning);

                        if (debug) {
                            setCharacteristicValue(characteristic.getId(), reading.value.toString(), debug);
                        } else {
                            Queue<Reading> queue;
                            if (readings.containsKey(reading.meaning)) {
                                queue = readings.get(reading.meaning);
                            } else {
                                // Init queue and add dummy values
                                queue = EvictingQueue.create(READING_HISTORY);
                                for (int i = 0; i < READING_HISTORY; i++) {
                                    queue.add(new Reading(0, 0, reading.meaning, reading.path, ""));
                                }
                            }

                            queue.add(reading);
                            readings.put(reading.meaning, queue);
                            latestReadings.put(reading.meaning, reading);

                            setCharacteristicValue(characteristic.getId(), reading.toString(), debug);
                        }
                    }
                });

        subscriptions.put(characteristic, subscription);
        setSubscribing(true);
        if (getReadingObservable() != null)
            getReadingObservable().connect();
        return subscription;
    }

    /**
     * Stops subscription to a characteristic with a given {@code id}
     *
     * @param context        context
     * @param characteristic characteristic
     */
    public void unsubscribe(final Context context, ECharacteristic characteristic) {
        Log.d(TAG, "Unsubscribe " + characteristic.getId());

        Subscription subscription = subscriptions.get(characteristic);

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscriptions.remove(characteristic);
        }

        setSubscribing(false);
        setReadingObservable(null);
        MappingController.getInstance(context).flangeAll();
    }

    public Subscription write(final EService service, final ECharacteristic characteristic, final Object value) {
        if (value instanceof Boolean) {
            return write(service, characteristic, ((Boolean) value).booleanValue());
        } else if (value instanceof String) {
            return write(service, characteristic, value.toString());
        }

        return null;
    }

    public Subscription write(final EService service, final ECharacteristic characteristic, final String value) {
        Log.d(TAG, "Write " + characteristic.getId() + " : " + value);

        return write(service, characteristic, value.getBytes());
    }

    public Subscription write(final EService service, final ECharacteristic characteristic, final boolean value) {
        Log.d(TAG, "Write " + characteristic.getId() + " : " + BleUtils.bytesToHex(value ? new byte[]{0x01} : new byte[]{0x00}));

        return write(service, characteristic, value ? new byte[]{0x01} : new byte[]{0x00});
    }

    /*
     * Writes a single value to a characteristic
     *
     * @param serviceId uuid of the service
     * @param characteristicId uuid of the characteristic
     * @param value to send
     * @return subscription
     */
    public Subscription write(final EService service, final ECharacteristic characteristic, final byte[] value) {
        return connect()
                .flatMap(new Func1<BaseService, Observable<BluetoothGattCharacteristic>>() {
                    @Override
                    public Observable<BluetoothGattCharacteristic> call(BaseService baseService) {
                        return baseService.write(value, service.getId(), characteristic.getId());
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        disconnect();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BluetoothGattCharacteristic>() {
                    @Override
                    public void onCompleted() {
                        reading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(BluetoothGattCharacteristic characteristic) {
                        Log.d(TAG, characteristic.getUuid() + " " + BleUtils.bytesToHex(characteristic.getValue()));
                    }
                });
    }

    /**
     * Clears ble cache
     */
    public void refreshCache() {
        try {
            if (gatt != null) {
                Method localMethod = gatt.getClass().getMethod("refresh");
                if (localMethod != null)
                    ocListener.onCacheCleared((Boolean) localMethod.invoke(gatt));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        ocListener.onCacheCleared(false);
    }

    public BluetoothGattCharacteristic getCharacteristic(ECharacteristic characteristic) {
        for (BluetoothGattCharacteristic c : getCharacteristics()) {
            if (c.getUuid().toString().equals(characteristic.getId())) {
                return c;
            }
        }

        return null;
    }

    public void setCharacteristicValue(String id, String value, boolean debug) {
        if (debug) {
            if (ocListener != null) ocListener.onChange(BleDevice.this, id, value);
        } else {
            for (BluetoothGattCharacteristic c : getCharacteristics()) {
                if (c.getUuid().toString().contains(id)) {
                    c.setValue(value);
                    if (ocListener != null) ocListener.onChange(BleDevice.this);
                }
            }
        }
    }

    public void save(Context context) {
        Realm realm = new MerlotRealm(context).getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                Log.d(TAG, "Save device");
                bgRealm.copyToRealmOrUpdate(BleDevice.this);
            }
        });
        realm.close();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("type=").append(this.getType()).append(", \n");
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("services=\n");

        if (getServices() != null) {
            for (BluetoothGattService service : getServices()) {
                sb.append("  service ").append(service.getUuid().toString().substring(4, 8)).append("\n");
                for (BluetoothGattCharacteristic chara : service.getCharacteristics()) {
                    sb.append("  characteristic ").append(chara.getUuid().toString().substring(4, 8)).append("\n");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        return type == bleDevice.type && (address != null ? address.equals(bleDevice.address) : bleDevice.address == null && !(name != null ? !name.equals(bleDevice.name) : bleDevice.name != null));
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    public boolean containsCharacteristic(ECharacteristic characteristic) {
        for (BluetoothGattCharacteristic c : getCharacteristics()) {
            if (c.getUuid().toString().equals(characteristic.getId())) {
                return true;
            }
        }

        return false;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public EDevice getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getRssi() { return rssi; }

    /*
    public BluetoothGatt getGatt() {
        return gatt;
    }
    */

    public void setGatt(BluetoothGatt gatt) {
        this.gatt = gatt;
    }

    public List<BluetoothGattService> getServices() {
        return services;
    }

    public void setServices(List<BluetoothGattService> services) {
        this.services = services;
    }

    public List<BluetoothGattCharacteristic> getCharacteristics() {
        if (this.characteristics == null || this.characteristics.isEmpty())
            initCharacteristics();

        return characteristics;
    }

    private void initCharacteristics() {
        List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
        if (getServices() != null) {
            for (BluetoothGattService service : getServices()) {
                if (service.getCharacteristics() != null) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        characteristics.add(characteristic);
                    }
                }
            }
        }

        this.characteristics = characteristics;
    }

    public void setCharacteristics(List<BluetoothGattCharacteristic> characteristics) {
        this.characteristics = characteristics;
    }

    public Map<String, Queue<Reading>> getReadings() {
        return readings;
    }

    public Map<String, Reading> getLatestReadings() {
        return latestReadings;
    }

    /*
    public Map<ECharacteristic, Subscription> getSubscriptions() {
        return subscriptions;
    }
    */

    public void setValues(Map<String, Queue<Reading>> readings) {
        this.readings = readings;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public boolean isSubscribing() {
        return subscribing;
    }

    public void setSubscribing(boolean subscribing) {
        this.subscribing = subscribing;
    }

    public boolean isAutoConnectEnabled() {
        return autoConnectEnabled;
    }

    public synchronized void setAutoConnectEnabled(boolean autoConnectEnabled) {
        Log.d(TAG, "Set Auto-connect enabled " + this.autoConnectEnabled);

        this.autoConnectEnabled = autoConnectEnabled;

        if (ocListener != null) {
            ocListener.onChange(this, autoConnectEnabled ? R.string.auto_connect_enabled : R.string.auto_connect_disabled);
        }
    }

    public ConnectableObservable<Reading> getReadingObservable() {
        return readingObservable;
    }

    public void setReadingObservable(ConnectableObservable<Reading> readingObservable) {
        this.readingObservable = readingObservable;
    }

    public void setOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnChangeListener {
        void onChange(BleDevice device);

        void onChange(BleDevice device, int text);

        void onChange(BleDevice device, String characteristic, String value);

        void onCacheCleared(boolean success);
    }

    public void registerOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }

    // </editor-fold>
}
