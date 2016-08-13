package de.interoberlin.merlot_android.model.config.repository;

import de.interoberlin.merlot_android.model.config.EFormat;

public class Characteristic {
    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;
    private String name;
    private EFormat format;
    private ERead read = ERead.ONCE;

    public enum ERead {
        NEVER,
        ONCE,
        SUBSCRIBE,
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    @Override
    public String toString() {
        return "id=" + this.getId() + ", \n" +
                "name=" + this.getName() + ", \n" +
                "format=" + this.getFormat() + ", \n" +
                "read=" + this.getRead() + ", \n";
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

    public EFormat getFormat() {
        return format;
    }

    public void setFormat(EFormat format) {
        this.format = format;
    }

    public ERead getRead() {
        return read;
    }

    /*
    public void setRead(ERead read) {
        this.read = read;
    }
    */

    // </editor-fold>
}
