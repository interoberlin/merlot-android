package de.interoberlin.merlot_android.model.repository;

public enum EDescriptor {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    DATA_NOTIFICATIONS("00002902-0000-1000-8000-00805f9b34fb");

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private String id;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EDescriptor(String id) {
        this.id = id;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static EDescriptor fromId(String id) {
        for (EDescriptor s : EDescriptor.values()) {
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

    // </editor-fold>
}
