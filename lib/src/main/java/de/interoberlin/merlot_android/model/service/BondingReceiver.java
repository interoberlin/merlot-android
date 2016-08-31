package de.interoberlin.merlot_android.model.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import de.interoberlin.merlot_android.model.util.DeviceCompatibilityUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED;
import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE;
import static android.bluetooth.BluetoothDevice.EXTRA_DEVICE;
import static rx.Observable.just;

class BondingReceiver {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BondingReceiver.class.getSimpleName();

    private Context context;

    static class BondingFunc1 implements Func1<BluetoothGatt, Observable<? extends BluetoothGatt>> {
        private Context context;

        BondingFunc1(Context context) {
            this.context = context;
        }

        @Override
        public Observable<? extends BluetoothGatt> call(final BluetoothGatt gatt) {
            int state = gatt.getDevice().getBondState();

            if (state == BluetoothDevice.BOND_BONDED) {
                return just(gatt);
            } else if (state == BluetoothDevice.BOND_BONDING) {
                return BondingReceiver.subscribeForBondStateChanges(context, gatt);
            } //else if (state == BluetoothDevice.BOND_NONE) {

            Observable<BluetoothGatt> bluetoothGattObservable =
                    BondingReceiver.subscribeForBondStateChanges(context, gatt);
            DeviceCompatibilityUtils.createBond(gatt.getDevice());
            return bluetoothGattObservable;
        }
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    static Observable<BluetoothGatt> subscribeForBondStateChanges(final Context context, final BluetoothGatt gatt) {
        Log.v(TAG, "Subscribe for bond state changes");
        return Observable.create(new Observable.OnSubscribe<BluetoothGatt>() {
            @Override
            public void call(final Subscriber<? super BluetoothGatt> subscriber) {
                context.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        BluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
                        int bondState = intent.getIntExtra(EXTRA_BOND_STATE, -1);
                        // int previousBondState = intent.getIntExtra(EXTRA_PREVIOUS_BOND_STATE, -1);

                        // skip other devices
                        if (!device.equals(gatt.getDevice()))
                            return;

                        if (bondState == BOND_BONDED) {
                            context.unregisterReceiver(this);
                            subscriber.onNext(gatt);
                            subscriber.onCompleted();
                        }

                        /*if (previousBondState == BOND_BONDING && bondState == BOND_NONE) {
                            RelayrApp.get().unregisterReceiver(this);
                            subscriber.onError(new Exception("Not bonded"));
                        }*/
                    }
                }, new IntentFilter(ACTION_BOND_STATE_CHANGED));
            }
        });
    }

    // </editor-fold>
}
