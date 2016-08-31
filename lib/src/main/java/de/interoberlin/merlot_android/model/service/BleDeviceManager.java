package de.interoberlin.merlot_android.model.service;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import rx.Subscriber;

public class BleDeviceManager {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BleDeviceManager.class.getSimpleName();

    private final Map<String, BleDevice> discoveredDevices = new ConcurrentHashMap<>();
    private final Map<Long, Subscriber<? super List<BleDevice>>> devicesSubscriberMap = new ConcurrentHashMap<>();

    private static BleDeviceManager instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    private BleDeviceManager() {
    }

    public static BleDeviceManager getInstance() {
        if (instance == null) {
            instance = new BleDeviceManager();
        }

        return instance;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public void addSubscriber(Long key, Subscriber<? super List<BleDevice>> devicesSubscriber) {
        Log.d(TAG, "Add subscriber " + key);
        devicesSubscriberMap.put(key, devicesSubscriber);
        if (!discoveredDevices.isEmpty()) devicesSubscriber.onNext(getDiscoveredDevices());
    }

    // TODO EXTREMELY IMPORTANT METHOD
    public void addDiscoveredDevice(BleDevice device) {
        Log.d(TAG, "Add discovered device " + device.getName());
        discoveredDevices.remove(device.getAddress());
        discoveredDevices.put(device.getAddress(), device);

        for (Subscriber<? super List<BleDevice>> devicesSubscriber : devicesSubscriberMap.values())
            devicesSubscriber.onNext(getDiscoveredDevices());
    }

    public void removeDevice(BleDevice device) {
        Log.d(TAG, "Remove device " + device.getName());
        discoveredDevices.remove(device.getAddress());
    }

    public void removeSubscriber(Long key) {
        Log.d(TAG, "Remove subscriber " + key);
        devicesSubscriberMap.remove(key);
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setter">

    boolean isDeviceDiscovered(String address) {
        return discoveredDevices.containsKey(address);
    }

    boolean isDeviceDiscovered(BleDevice device) {
        return isDeviceDiscovered(device.getAddress());
    }

    List<BleDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }

    public boolean isThereAnySubscriber() {
        return !devicesSubscriberMap.isEmpty();
    }

    // </editor-fold>
}
