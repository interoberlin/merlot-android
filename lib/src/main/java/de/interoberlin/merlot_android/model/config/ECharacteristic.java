package de.interoberlin.merlot_android.model.config;

import android.content.Context;

import de.interoberlin.merlot_android.model.config.repository.RepositoryMapper;

public enum ECharacteristic {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    DEVICE_NAME(EService.GENERIC_ACCESS, "00002a00-0000-1000-8000-00805f9b34fb"),
    APPEARANCE (EService.GENERIC_ACCESS, "00002a01-0000-1000-8000-00805f9b34fb"),
    PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS(EService.GENERIC_ACCESS, "00002a04-0000-1000-8000-00805f9b34fb"),
    RECONNECTION_ADDRESS(EService.GENERIC_ACCESS, "00002a03-0000-1000-8000-00805f9b34fb"),

    SERVICE_CHANGED(EService.GENERIC_ATTRIBUTE, "00002a05-0000-1000-8000-00805f9b34fb"),

    MANUFACTURER_NAME(EService.DEVICE_INFORMATION, "00002a29-0000-1000-8000-00805f9b34fb"),
    HARDWARE_REVISION(EService.DEVICE_INFORMATION, "00002a27-0000-1000-8000-00805f9b34fb"),
    FIRMWARE_REVISION(EService.DEVICE_INFORMATION, "00002a26-0000-1000-8000-00805f9b34fb"),

    BATTERY_LEVEL(EService.BATTERY_LEVEL, "00002a19-0000-1000-8000-00805f9b34fb"),

    SERVICE_ONBOARDING(EService.CONNECTED_TO_MASTER_MODULE, "00002001-0000-1000-8000-00805f9b34fb"),
    SENSOR_ID(EService.CONNECTED_TO_MASTER_MODULE, "00002010-0000-1000-8000-00805f9b34fb"),
    PASS_KEY(EService.CONNECTED_TO_MASTER_MODULE, "00002018-0000-1000-8000-00805f9b34fb"),
    ONBOARDING_FLAG(EService.CONNECTED_TO_MASTER_MODULE, "00002019-0000-1000-8000-00805f9b34fb"),

    BEACON_FREQUENCY(EService.DIRECT_CONNECTION, "00002011-0000-1000-8000-00805f9b34fb"),
    FREQUENCY(EService.DIRECT_CONNECTION, "00002012-0000-1000-8000-00805f9b34fb"),
    LED_STATE(EService.DIRECT_CONNECTION, "00002013-0000-1000-8000-00805f9b34fb"),
    THRESHOLD(EService.DIRECT_CONNECTION, "00002014-0000-1000-8000-00805f9b34fb"),
    CONFIGURATION(EService.DIRECT_CONNECTION, "00002015-0000-1000-8000-00805f9b34fb"),
    DATA(EService.DIRECT_CONNECTION, "00002016-0000-1000-8000-00805f9b34fb"),
    SEND_COMMAND(EService.DIRECT_CONNECTION, "00002017-0000-1000-8000-00805f9b34fb"),

    LED_COLOR(EService.SENTIENT_LIGHT, "00003001-0000-1000-8000-00805f9b34fb"),

    HEART_RATE(EService.HEART_RATE, "00002a37-0000-1000-8000-00805f9b34fb");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private EService service;
    private String id;

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    ECharacteristic(EService service, String id) {
        this.service = service;
        this.id = id;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static ECharacteristic fromId(String id) {
        for (ECharacteristic c : ECharacteristic.values()) {
            if (c.getId().equals(id))
                return c;
        }

        return null;
    }

    public EFormat getFormat(Context context) {
        if (RepositoryMapper.getInstance(context).isKnownCharacteristic(id)) {
            return RepositoryMapper.getInstance(context).getCharacteristicById(id).getFormat();
        }

        return null;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public EService getService() {
        return service;
    }

    public void setService(EService service) {
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // </editor-fold>
}
