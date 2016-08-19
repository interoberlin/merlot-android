package de.interoberlin.merlot_android.model.mapping;

public class Source {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private String address;
    private String characteristic;
    private String node;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("address=").append(this.getAddress()).append(", \n");
        sb.append("characteristic=").append(this.getCharacteristic().toString());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    // </editor-fold>
}
