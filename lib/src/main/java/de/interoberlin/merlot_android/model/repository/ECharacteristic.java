package de.interoberlin.merlot_android.model.repository;

public enum ECharacteristic {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    DEVICE_NAME(EService.GENERIC_ACCESS, "00002a00-0000-1000-8000-00805f9b34fb", "Device name"),
    APPEARANCE(EService.GENERIC_ACCESS, "00002a01-0000-1000-8000-00805f9b34fb", "Appearance"),
    PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS(EService.GENERIC_ACCESS, "00002a04-0000-1000-8000-00805f9b34fb", "Peripheral preferred connection parameters"),
    RECONNECTION_ADDRESS(EService.GENERIC_ACCESS, "00002a03-0000-1000-8000-00805f9b34fb", "Reconnection address"),

    SERVICE_CHANGED(EService.GENERIC_ATTRIBUTE, "00002a05-0000-1000-8000-00805f9b34fb", "Service changed"),

    MANUFACTURER_NAME(EService.DEVICE_INFORMATION, "00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer name"),
    HARDWARE_REVISION(EService.DEVICE_INFORMATION, "00002a27-0000-1000-8000-00805f9b34fb", "Hardware revision"),
    FIRMWARE_REVISION(EService.DEVICE_INFORMATION, "00002a26-0000-1000-8000-00805f9b34fb", "Firmware revision"),

    BATTERY_LEVEL(EService.BATTERY_LEVEL, "00002a19-0000-1000-8000-00805f9b34fb", "Battery level"),

    SERVICE_ONBOARDING(EService.CONNECTED_TO_MASTER_MODULE, "00002001-0000-1000-8000-00805f9b34fb", "Service onboarding"),
    SENSOR_ID(EService.CONNECTED_TO_MASTER_MODULE, "00002010-0000-1000-8000-00805f9b34fb", "Sensor ID"),
    PASS_KEY(EService.CONNECTED_TO_MASTER_MODULE, "00002018-0000-1000-8000-00805f9b34fb", "Pass key"),
    ONBOARDING_FLAG(EService.CONNECTED_TO_MASTER_MODULE, "00002019-0000-1000-8000-00805f9b34fb", "Onboarding flag"),

    BEACON_FREQUENCY(EService.DIRECT_CONNECTION, "00002011-0000-1000-8000-00805f9b34fb", "Beacon frequency"),
    FREQUENCY(EService.DIRECT_CONNECTION, "00002012-0000-1000-8000-00805f9b34fb", "Frequency"),
    LED_STATE(EService.DIRECT_CONNECTION, "00002013-0000-1000-8000-00805f9b34fb", "LED state"),
    THRESHOLD(EService.DIRECT_CONNECTION, "00002014-0000-1000-8000-00805f9b34fb", "Threshold"),
    CONFIGURATION(EService.DIRECT_CONNECTION, "00002015-0000-1000-8000-00805f9b34fb", "Configuration"),
    DATA(EService.DIRECT_CONNECTION, "00002016-0000-1000-8000-00805f9b34fb", "Data"),
    SEND_COMMAND(EService.DIRECT_CONNECTION, "00002017-0000-1000-8000-00805f9b34fb", "Send command"),

    HEART_RATE(EService.HEART_RATE, "00002a37-0000-1000-8000-00805f9b34fb", "Heart rate"),

    INTEROBERLIN_UART_TX(EService.INTEROBERLIN_UART, "00002002-0000-1000-8000-00805f9b34fb", "Interoberlin UART TX"),
    INTEROBERLIN_UART_RX(EService.INTEROBERLIN_UART, "00002003-0000-1000-8000-00805f9b34fb", "Interoberlin UART RX"),

    SENTIENT_LIGHT_FLOOR_SENSOR_TX(EService.SENTIENT_LIGHT_FLOOR_SENSOR, "00003002-0000-1000-8000-00805f9b34fb", "Sentient Light Floor Sensor TX"),
    SENTIENT_LIGHT_FLOOR_SENSOR_RX(EService.SENTIENT_LIGHT_FLOOR_SENSOR, "00003003-0000-1000-8000-00805f9b34fb", "Sentient Light Floor Sensor RX"),
    SENTIENT_LIGHT_LED_TX(EService.SENTIENT_LIGHT_LED, "00004002-0000-1000-8000-00805f9b34fb", "Sentient Light LED TX"),
    SENTIENT_LIGHT_LED_RX(EService.SENTIENT_LIGHT_LED, "00004003-0000-1000-8000-00805f9b34fb", "Sentient Light LED RX");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private EService service;
    private String id;
    private String name;

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    ECharacteristic(EService service, String id, String name) {
        this.service = service;
        this.id = id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // </editor-fold>
}
