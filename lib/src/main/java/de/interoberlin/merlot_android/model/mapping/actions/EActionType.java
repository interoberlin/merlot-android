package de.interoberlin.merlot_android.model.mapping.actions;

import java.util.ArrayList;
import java.util.List;

public enum EActionType {
    // <editor-fold defaultstate="collapsed" desc="Entries">

    WRITE_CHARACTERISTIC("write characteristic", WriteCharacteristicAction.class);

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Members">

    private final String name;
    private final Class c;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    EActionType(String name, Class c) {
        this.name = name;
        this.c = c;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    public static EActionType fromString(String name) {
        for (EActionType e : EActionType.values()) {
            if (e.getName().equals(name))
                return e;
        }

        return null;
    }

    public static List<String> getNamesList() {
        List names = new ArrayList<>();

        for (EActionType e : EActionType.values()) {
            names.add(e.getName());
        }

        return names;
    }

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
