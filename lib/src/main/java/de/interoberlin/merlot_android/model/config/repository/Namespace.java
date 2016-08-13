package de.interoberlin.merlot_android.model.config.repository;

import java.util.List;

public class Namespace {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;
    private String name;
    private List<Service> services;

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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    // </editor-fold>
}