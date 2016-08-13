package de.interoberlin.merlot_android.model.mapping.functions;

public class ThresholdFunction implements IFunction {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = ThresholdFunction.class.getSimpleName();

    private float minValue;
    private float maxValue;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    @Override
    public boolean isTriggered(float input) {
        return input >= minValue && input <= maxValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("minValue=").append(this.getMinValue()).append(", \n");
        sb.append("maxValue=").append(this.getMaxValue());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    // </editor-fold>
}