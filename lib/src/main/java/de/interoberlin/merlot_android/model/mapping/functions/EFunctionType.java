package de.interoberlin.merlot_android.model.mapping.functions;

import java.util.ArrayList;
import java.util.List;

public enum EFunctionType {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    THRESHOLD("threshold", ThresholdFunction.class);

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private final String name;
    private final Class c;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EFunctionType(String name, Class c) {
        this.name = name;
        this.c = c;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static List<String> getNamesList() {
        List names = new ArrayList<>();

        for (EFunctionType e : EFunctionType.values()) {
            names.add(e.getName());
        }

        return names;
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public String getName() {
        return name;
    }


    public Class getC() {
        return c;
    }

    // </editor-fold>
}
