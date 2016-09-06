package de.interoberlin.merlot_android.modules;

import de.interoberlin.merlot_android.model.ble.BleDevice;
import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {BleDevice.class})
public class BleDeviceModule {
}
