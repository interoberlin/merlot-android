package de.interoberlin.merlot_android.model.repository;

public enum EDevice {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    WUNDERBAR_HTU("ecf6cf94-cb07-43ac-a85e-dccf26b48c86", "WunderbarHTU"),
    WUNDERBAR_GYRO("173c44b5-334e-493f-8eb8-82c8cc65d29f","WunderbarGYRO"),
    WUNDERBAR_LIGHT("a7ec1b21-8582-4304-b1cf-15a1fc66d1e8","WunderbarLIGHT"),
    WUNDERBAR_MIC("4f38b6c6-a8e9-4f93-91cd-2ac4064b7b5a","WunderbarMIC"),
    WUNDERBAR_BRIDG("ebd828dd-250c-4baf-807d-69d85bed065b","WunderbarBRIDG"),
    WUNDERBAR_IR("bab45b9c-1c44-4e71-8e98-a321c658df47","WunderbarIR"),

    INTEROBERLIN_SENTIENT_LIGHT("be73d68c-1d33-11e6-b6ba-3e1d05defe78","InteroberlinSentientLight"),
    NRFDUINO("e3598c73-6f6e-4be6-9861-ee6fb9378dcc","nRFduino"),
    UNKNOWN("", "");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private final String name;
    private final String id;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EDevice(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static EDevice fromString(String name) {
        if (name != null) {
            for (EDevice b : EDevice.values()) {
                if (name.equalsIgnoreCase(b.getName())) {
                    return b;
                }
            }
        }

        return EDevice.UNKNOWN;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // </editor-fold>
}