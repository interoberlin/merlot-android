package de.interoberlin.merlot_android.model.config.repository;

import java.util.List;

public class Service {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;
    private String name;
    private List<Characteristic> characteristics;

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

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(List<Characteristic> characteristics) {
        this.characteristics = characteristics;
    }

    // </editor-fold>
}
