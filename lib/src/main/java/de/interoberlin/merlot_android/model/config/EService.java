package de.interoberlin.merlot_android.model.config;

public enum EService {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    GENERIC_ACCESS("00001800-0000-1000-8000-00805f9b34fb"),
    GENERIC_ATTRIBUTE("00001801-0000-1000-8000-00805f9b34fb"),
    DEVICE_INFORMATION("0000180a-0000-1000-8000-00805f9b34fb"),
    BATTERY_LEVEL("0000180f-0000-1000-8000-00805f9b34fb"),

    CONNECTED_TO_MASTER_MODULE("00002000-0000-1000-8000-00805f9b34fb"),
    DIRECT_CONNECTION("00002002-0000-1000-8000-00805f9b34fb"),

    HEART_RATE("0000180d-0000-1000-8000-00805f9b34fb"),

    SENTIENT_LIGHT_FLOOR_SENSOR("00003001-0000-1000-8000-00805f9b34fb"),
    SENTIENT_LIGHT_LED("00004001-0000-1000-8000-00805f9b34fb");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EService(String id) {
        this.id = id;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static EService fromId(String id) {
        for (EService s : EService.values()) {
            if (s.getId().equals(id))
                return s;
        }

        return null;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // </editor-fold>
}
