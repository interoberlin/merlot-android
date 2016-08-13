package de.interoberlin.merlot_android.model.mapping.functions;

public enum EFunctionType {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    THRESHOLD(ThresholdFunction.class);

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private final Class c;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EFunctionType(Class c) {
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
