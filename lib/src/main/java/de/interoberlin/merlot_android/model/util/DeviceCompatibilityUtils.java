package de.interoberlin.merlot_android.model.util;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

public class DeviceCompatibilityUtils {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private static final String TAG = DeviceCompatibilityUtils.class.getSimpleName();

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    private static boolean isSdk19() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean createBond(BluetoothDevice device) {
        if (isSdk19()) return doCreateBond(device);
        return callMethod(device, "createBond");
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean doCreateBond(BluetoothDevice device) {
        // if (!RelayrSdk.isPermissionGrantedBluetoothAdmin()) return false;
        return device.createBond();
    }

    public static boolean removeBond(BluetoothDevice device) {
        return callMethod(device, "removeBond");
    }

    public static boolean refresh(BluetoothGatt gatt) {
        try {
            Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(gatt);
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occurred while performing: refresh", localException.getCause());
        }
        return false;
    }

    private static boolean callMethod(BluetoothDevice device, String methodName) {
        try {
            Method localMethod = device.getClass().getMethod(methodName, (Class[]) null);
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(device);
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occurred while performing: " + methodName, localException.getCause());
        }
        return false;
    }

    // </editor-fold>
}
