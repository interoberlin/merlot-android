package de.interoberlin.merlot_android.modules;

import de.interoberlin.merlot_android.model.mapping.Mapping;
import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {Mapping.class})
public class MappingModule {
}
