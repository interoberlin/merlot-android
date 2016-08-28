package de.interoberlin.merlot_android.model.repository;

public enum EService {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    GENERIC_ACCESS("00001800-0000-1000-8000-00805f9b34fb", "Generic access"),
    GENERIC_ATTRIBUTE("00001801-0000-1000-8000-00805f9b34fb", "Generic attribute"),
    DEVICE_INFORMATION("0000180a-0000-1000-8000-00805f9b34fb", "Device information"),
    BATTERY_LEVEL("0000180f-0000-1000-8000-00805f9b34fb", "Battery level"),

    CONNECTED_TO_MASTER_MODULE("00002000-0000-1000-8000-00805f9b34fb", "Connected to master module"),
    DIRECT_CONNECTION("00002002-0000-1000-8000-00805f9b34fb", "Direct connection"),

    HEART_RATE("0000180d-0000-1000-8000-00805f9b34fb", "Heart rate"),

    INTEROBERLIN_UART("00002001-0000-1000-8000-00805f9b34fb", "Interoberlin UART"),
    SENTIENT_LIGHT_FLOOR_SENSOR("00003001-0000-1000-8000-00805f9b34fb", "Sentient light floor sensor"),
    SENTIENT_LIGHT_LED("00004001-0000-1000-8000-00805f9b34fb", "Sentient Light LED");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;
    private String name;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EService(String id, String name) {
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // </editor-fold>
}
