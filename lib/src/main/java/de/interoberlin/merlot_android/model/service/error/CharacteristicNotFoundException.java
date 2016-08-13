package de.interoberlin.merlot_android.model.service.error;

public class CharacteristicNotFoundException extends Exception {
    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    public CharacteristicNotFoundException(String characteristic) {
        super(characteristic + " Characteristic not found.");
    }

    // </editor-fold>
}
