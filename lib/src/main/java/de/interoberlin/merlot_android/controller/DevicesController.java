package de.interoberlin.merlot_android.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.ble.BleDevicesScanner;
import de.interoberlin.merlot_android.model.ble.BleScannerFilter;
import de.interoberlin.merlot_android.model.ble.BluetoothLeService;
import de.interoberlin.merlot_android.model.service.BleDeviceManager;
import io.realm.Realm;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

public class DevicesController {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = DevicesController.class.getSimpleName();

    // Model
    private Map<String, BleDevice> scannedDevices;
    private Map<String, BleDevice> attachedDevices;
    private BleDeviceManager bluetoothDeviceManager;
    private BleDevicesScanner bleDeviceScanner;

    private static DevicesController instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    private DevicesController() {
        this.scannedDevices = new HashMap<>();
        this.attachedDevices = new HashMap<>();
    }

    public static DevicesController getInstance() {
        if (instance == null) {
            instance = new DevicesController();
        }

        return instance;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Starts a scan for BLE devices
     *
     * @param context  context
     * @param callback callback
     */
    public void startScan(Context context, BleScannerFilter.BleFilteredScanCallback callback) {
        Log.d(TAG, "Start scan");

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
        } else {
            scan(context, bluetoothManager, bluetoothAdapter, callback)
                    .filter(new Func1<List<BleDevice>, Boolean>() {
                        @Override
                        public Boolean call(List<BleDevice> bleDevices) {
                            return false;
                        }
                    })
                    .map(new Func1<List<BleDevice>, BleDevice>() {
                        @Override
                        public BleDevice call(List<BleDevice> bleDevices) {
                            for (BleDevice device : bleDevices) {
                                return device;
                            }

                            return null; // will never happen since it's been filtered out before
                        }
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<BleDevice>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(BleDevice device) {
                        }
                    });
        }
    }

    /**
     * Stops a scan for BLE devices
     */
    public void stopScan() {
        Log.d(TAG, "Stop scan");

        if (bleDeviceScanner != null && bleDeviceScanner.isScanning())
            bleDeviceScanner.stop();
    }

    /**
     * Performs a scan for BLE devices
     *
     * @param callback         callback
     * @param bluetoothManager bluetooth manager
     * @param bluetoothAdapter bluetooth adapter
     */
    private Observable<List<BleDevice>> scan(Context context, BluetoothManager bluetoothManager, BluetoothAdapter bluetoothAdapter, BleScannerFilter.BleFilteredScanCallback callback) {
        for (BluetoothDevice d : bluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER)) {
            Log.v(TAG, "Already connected " + d.getName());
        }

        this.bluetoothDeviceManager = BleDeviceManager.getInstance();
        BleScannerFilter scannerFilter = new BleScannerFilter(context, bluetoothDeviceManager, callback);
        this.bleDeviceScanner = new BleDevicesScanner(context, bluetoothAdapter, scannerFilter);

        final long key = System.currentTimeMillis();
        return Observable.create(new Observable.OnSubscribe<List<BleDevice>>() {
            @Override
            public void call(Subscriber<? super List<BleDevice>> subscriber) {
                bluetoothDeviceManager.addSubscriber(key, subscriber);
                bleDeviceScanner.start();
            }
        }).map(new Func1<List<BleDevice>, List<BleDevice>>() {
            @Override
            public List<BleDevice> call(List<BleDevice> bleDevices) {
                List<BleDevice> devices = new ArrayList<>();
                for (BleDevice device : bleDevices)
                    devices.add(device);
                return devices;
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                bluetoothDeviceManager.removeSubscriber(key);
                if (!bluetoothDeviceManager.isThereAnySubscriber()) bleDeviceScanner.stop();
            }
        });
    }

    /**
     * Attaches a device
     *
     * @param context    context
     * @param ocListener on change listener
     * @param service    BLE service
     * @param device     device
     * @return true, if it worked
     */
    public boolean attach(Context context, BleDevice.OnChangeListener ocListener, BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Attach " + device.getName());

        if (service != null) {
            service.connect(device.getAddress());
            if (scannedDevices.containsKey(device.getAddress()))
                scannedDevices.remove(device.getAddress());
            attachedDevices.put(device.getAddress(), device);

            device.registerOnChangeListener(ocListener);

            MappingController.getInstance(context).flangeAll();

            return true;
        }

        return false;
    }

    /**
     * Detaches a device
     *
     * @param context context
     * @param service BLE service
     * @param device  device
     * @return true, if it worked
     */
    public boolean detach(Context context, BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Detach" + device.getName());

        if (service != null) {
            device.disconnect();
            device.close();
            service.disconnect();
            service.close();

            if (attachedDevices.containsKey(device.getAddress()))
                attachedDevices.remove(device.getAddress());

            MappingController.getInstance(context).flangeAll();

            return true;
        }

        return false;
    }

    /**
     * Disconnects from a {@code device}
     *
     * @param service service
     * @param device  device
     * @return true if it worked
     */
    public boolean disconnect(BluetoothLeService service, BleDevice device) {
        Log.d(TAG, "Disconnect " + device.getName());

        if (service != null) {
            device.disconnect();
            service.disconnect();
            return true;
        }

        return false;
    }

    /**
     * Toggles a auto-connect attribute of a device identified by a given {@code deviceAddress}
     *
     * @param deviceAddress device address
     */
    public void refreshCache(String deviceAddress) {
        refreshCache(getAttachedDeviceByAddress(deviceAddress));
    }

    /**
     * Refreshes cache of a given {@code device}
     *
     * @param device device
     */
    public void refreshCache(BleDevice device) {
        device.refreshCache();
    }

    /**
     * Toggles a auto-connect attribute of a given {@code device}
     *
     * @param device device
     */
    public void toggleAutoConnect(BleDevice device) {
        device.setAutoConnectEnabled(!device.isAutoConnectEnabled());
        device.save();
    }

    /**
     * Determines whether a {@code device}'s attribute {@code autoConnectEnabled} is set to true
     *
     * @param device device
     * @return true is auto-connect is enabled
     */
    public boolean isAutoConnectEnabled(BleDevice device) {
        Realm realm = Realm.getDefaultInstance();
        boolean autoConnectEnabled = realm.where(BleDevice.class)
                .equalTo("address", device.getAddress())
                .equalTo("autoConnectEnabled", true).
                        count() > 0;
        if (!realm.isClosed()) realm.close();

        return autoConnectEnabled;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public Map<String, BleDevice> getScannedDevices() {
        return scannedDevices;
    }

    public List<BleDevice> getScannedDevicesAsList() {
        return new ArrayList<>(getScannedDevices().values());
    }

    public Map<String, BleDevice> getAttachedDevices() {
        return attachedDevices;
    }

    public BleDevice getAttachedDeviceByAddress(String address) {
        return getAttachedDevices().get(address);
    }

    public List<BleDevice> getAttachedDevicesAsList() {
        return new ArrayList<>(getAttachedDevices().values());
    }

    // </editor-fold>
}
