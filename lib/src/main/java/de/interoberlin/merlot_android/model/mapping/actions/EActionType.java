package de.interoberlin.merlot_android.model.mapping.actions;

public enum EActionType {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    WRITE_CHARACTERISTIC(WriteCharacteristicAction.class);

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private final Class c;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EActionType(Class c) {
        this.c = c;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public Class getC() {
        return c;
    }

    // </editor-fold>
}
