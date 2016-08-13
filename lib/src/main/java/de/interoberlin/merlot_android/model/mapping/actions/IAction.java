package de.interoberlin.merlot_android.model.mapping.actions;

import de.interoberlin.merlot_android.model.mapping.Sink;

public interface IAction {
    void perform(Sink sink);
}
