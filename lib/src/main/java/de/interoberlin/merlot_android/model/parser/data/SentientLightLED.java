package de.interoberlin.merlot_android.model.parser.data;

import java.util.ArrayList;
import java.util.List;

public class SentientLightLED {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private List<LED> leds;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public SentientLightLED() {
        this.leds = new ArrayList<>();
    }

    // </editor-fold>

    // --------------------
    // Inner classes
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Inner classes">

    public class LED {
        private int index;
        private List<Integer> values;

        // --------------------
        // Constructors
        // --------------------

        public LED(int index, int v1, int v2, int v3) {
            this.index = index;
            this.values = new ArrayList<>();
            this.values.add(v1);
            this.values.add(v2);
            this.values.add(v3);
        }

        // --------------------
        // Getters / Setters
        // --------------------

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public List<Integer> getValues() {
            return values;
        }

        public void setValues(List<Integer> values) {
            this.values = values;
        }
    }

    // </editor-fold>

    // --------------------
    // Getters / Setter
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setter">

    public List<LED> getLeds() {
        return leds;
    }

    public void setLeds(List<LED> leds) {
        this.leds = leds;
    }

    // </editor-fold>
}
