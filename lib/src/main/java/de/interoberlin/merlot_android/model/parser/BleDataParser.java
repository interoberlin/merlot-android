package de.interoberlin.merlot_android.model.parser;

import android.bluetooth.BluetoothGattCharacteristic;

import com.google.gson.Gson;

import de.interoberlin.merlot_android.model.parser.data.AccelGyroscope;
import de.interoberlin.merlot_android.model.parser.data.LightColorProx;
import de.interoberlin.merlot_android.model.repository.ECharacteristic;
import de.interoberlin.merlot_android.model.repository.EDevice;

public abstract class BleDataParser {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = BleDataParser.class.getSimpleName();

    /**
     * Characteristic value format type uint8
     */
    public static final int FORMAT_UINT8 = 0x11;

    /**
     * Characteristic value format type uint16
     */
    public static final int FORMAT_UINT16 = 0x12;

    /**
     * Characteristic value format type uint32
     */
    public static final int FORMAT_UINT32 = 0x14;

    /**
     * Characteristic value format type sint8
     */
    public static final int FORMAT_SINT8 = 0x21;

    /**
     * Characteristic value format type sint16
     */
    public static final int FORMAT_SINT16 = 0x22;

    /**
     * Characteristic value format type sint32
     */
    public static final int FORMAT_SINT32 = 0x24;

    /**
     * Characteristic value format type sfloat (16-bit float)
     */
    public static final int FORMAT_SFLOAT = 0x32;

    /**
     * Characteristic value format type float (32-bit float)
     */
    public static final int FORMAT_FLOAT = 0x34;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static String getFormattedValue(EDevice type, ECharacteristic characteristic, byte[] value) {
        if (value == null) return "";

        switch (characteristic) {
            case DATA: {
                switch (type) {
                    case WUNDERBAR_LIGHT: {
                        return BleDataParser.getLIGHTSensorData(value);
                    }
                    case WUNDERBAR_GYRO: {
                        return BleDataParser.getGYROSensorData(value);
                    }
                    case WUNDERBAR_HTU: {
                        return BleDataParser.getHTUSensorData(value);
                    }
                    case WUNDERBAR_MIC: {
                        return BleDataParser.getMICSensorData(value);
                    }
                    case WUNDERBAR_BRIDG: {
                        return getBridgeSensorData(value);
                    }
                    case SENTIENT_LIGHT_FLOOR_SENSOR:
                    default:
                        return "";
                }
            }
            case BATTERY_LEVEL: {
                return String.valueOf(getIntValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0));
            }
            default: {
                return new String(value);
            }
        }
    }

    public static Integer getIntValue(byte[] value, int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) return null;

        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(value[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(value[offset], value[offset+1]);

            case FORMAT_UINT32:
                return unsignedBytesToInt(value[offset],   value[offset+1],
                        value[offset+2], value[offset+3]);
            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(value[offset]), 8);

            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(value[offset],
                        value[offset+1]), 16);

            case FORMAT_SINT32:
                return unsignedToSigned(unsignedBytesToInt(value[offset],
                        value[offset+1], value[offset+2], value[offset+3]), 32);
        }

        return null;
    }

    private static String getLIGHTSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EDevice.WUNDERBAR_LIGHT.getId();
        dataPackage.received = System.currentTimeMillis();

        int red = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);
        int green = (byteToUnsignedInt(value[3]) << 8) | byteToUnsignedInt(value[2]);
        int blue = (byteToUnsignedInt(value[5]) << 8) | byteToUnsignedInt(value[4]);
        LightColorProx.Color color = new LightColorProx.Color(red, green, blue);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "color", "", color));

        int proximity = (byteToUnsignedInt(value[9]) << 8) | byteToUnsignedInt(value[8]);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "proximity", "", proximity));

        int luminosity = (byteToUnsignedInt(value[7]) << 8) | byteToUnsignedInt(value[6]);
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "luminosity", "", luminosity));

        return new Gson().toJson(dataPackage);
    }

    public static LightColorProx.Color getColor(String value) {
        return new Gson().fromJson(value, LightColorProx.Color.class);
    }

    private static String getGYROSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EDevice.WUNDERBAR_GYRO.getId();
        dataPackage.received = System.currentTimeMillis();

        int gyroscopeX = byteToUnsignedInt(value[0]) |
                (byteToUnsignedInt(value[1]) << 8) |
                (byteToUnsignedInt(value[2]) << 16) |
                (byteToUnsignedInt(value[3]) << 24);
        int gyroscopeY = byteToUnsignedInt(value[4]) |
                (byteToUnsignedInt(value[5]) << 8) |
                (byteToUnsignedInt(value[6]) << 16) |
                (byteToUnsignedInt(value[7]) << 24);
        int gyroscopeZ = byteToUnsignedInt(value[8]) |
                (byteToUnsignedInt(value[9]) << 8) |
                (byteToUnsignedInt(value[10]) << 16) |
                (byteToUnsignedInt(value[11]) << 24);

        int accelerationX = (byteToUnsignedInt(value[13]) << 8) | byteToUnsignedInt(value[12]);
        int accelerationY = (byteToUnsignedInt(value[15]) << 8) | byteToUnsignedInt(value[14]);
        int accelerationZ = (byteToUnsignedInt(value[17]) << 8) | byteToUnsignedInt(value[16]);

        AccelGyroscope.Acceleration acceleration = new AccelGyroscope.Acceleration();
        acceleration.x = (float) accelerationX / 100.0f;
        acceleration.y = (float) accelerationY / 100.0f;
        acceleration.z = (float) accelerationZ / 100.0f;
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "acceleration", "", acceleration));

        AccelGyroscope.AngularSpeed angularSpeed = new AccelGyroscope.AngularSpeed();
        angularSpeed.x = (float) gyroscopeX / 100.0f;
        angularSpeed.y = (float) gyroscopeY / 100.0f;
        angularSpeed.z = (float) gyroscopeZ / 100.0f;
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "angularSpeed", "", angularSpeed));

        return new Gson().toJson(dataPackage);
    }

    public static AccelGyroscope.Acceleration getAcceleration(String value) {
        return new Gson().fromJson(value, AccelGyroscope.Acceleration.class);
    }

    public static AccelGyroscope.AngularSpeed getAngularSpeed(String value) {
        return new Gson().fromJson(value, AccelGyroscope.AngularSpeed.class);
    }

    private static String getHTUSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EDevice.WUNDERBAR_HTU.getId();
        dataPackage.received = System.currentTimeMillis();

        int temperature = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);
        int humidity = (byteToUnsignedInt(value[3]) << 8) | byteToUnsignedInt(value[2]);

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "humidity", "", (int) ((float) humidity / 100.0f)));
        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "temperature", "", (float) temperature / 100.0f));
        return new Gson().toJson(dataPackage);
    }

    private static String getMICSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EDevice.WUNDERBAR_MIC.getId();
        dataPackage.received = System.currentTimeMillis();

        int noiseLevel = (byteToUnsignedInt(value[1]) << 8) | byteToUnsignedInt(value[0]);

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "noiseLevel", "", noiseLevel));
        return new Gson().toJson(dataPackage);
    }

    private static String getBridgeSensorData(byte[] value) {
        DataPackage dataPackage = new DataPackage();
        dataPackage.modelId = EDevice.WUNDERBAR_BRIDG.getId();
        dataPackage.received = System.currentTimeMillis();

        dataPackage.readings.add(new DataPackage.Data(dataPackage.received, "up_ch_payload", "", value));
        return new Gson().toJson(dataPackage);
    }

    private static int byteToUnsignedInt(byte b) {
        return (int) b & 0xff;
    }

    private static int getTypeLen(int formatType) {
        return formatType & 0xF;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    private static float bytesToFloat(byte b0, byte b1) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
        return (float)(mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + (unsignedByteToInt(b1) << 8)
                + (unsignedByteToInt(b2) << 16), 24);
        return (float)(mantissa * Math.pow(10, b3));
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size-1)) != 0) {
            unsigned = -1 * ((1 << size-1) - (unsigned & ((1 << size-1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert an integer into the signed bits of a given length.
     */
    private static int intToSignedBits(int i, int size) {
        if (i < 0) {
            i = (1 << size-1) + (i & ((1 << size-1) - 1));
        }
        return i;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    // </editor-fold>
}
