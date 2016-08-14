package de.interoberlin.merlot_android;

import android.content.Context;

import de.interoberlin.merlot_android.modules.DevicesModule;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class DevicesRealm {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private final RealmConfiguration realmConfig;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public DevicesRealm(Context context) {
        realmConfig = new RealmConfiguration.Builder(context)
                .name("de.interoberlin.merlot")
                .modules(new DevicesModule())
                .build();

        Realm.deleteRealm(realmConfig);
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public Realm getRealm() {
        return Realm.getInstance(realmConfig);
    }

    // </editor-fold>
}