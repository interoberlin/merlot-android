package de.interoberlin.merlot_android;

import android.content.Context;

import de.interoberlin.merlot_android.modules.DevicesModule;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DevicesRealm {
    private final RealmConfiguration realmConfig;
    private Realm realm;

    public DevicesRealm(Context context) {
        realmConfig = new RealmConfiguration.Builder(context)
                .name("de.interoberlin.merlot")
                .modules(new DevicesModule())
                .build();

        // Reset Realm
        Realm.deleteRealm(realmConfig);
    }

    public void open() {
        realm = Realm.getInstance(realmConfig);
    }

    public void close() {
        realm.close();
    }
}