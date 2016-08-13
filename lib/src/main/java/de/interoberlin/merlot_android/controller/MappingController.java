package de.interoberlin.merlot_android.controller;

import android.content.Context;

import com.google.gson.GsonBuilder;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.interoberlin.mate.lib.model.Log;
import de.interoberlin.merlot_android.R;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.mapping.Mapping;
import de.interoberlin.merlot_android.model.mapping.Sink;
import de.interoberlin.merlot_android.model.mapping.Source;
import de.interoberlin.merlot_android.model.mapping.actions.IAction;
import de.interoberlin.merlot_android.model.mapping.actions.IActionDeserializer;
import de.interoberlin.merlot_android.model.mapping.functions.IFunction;
import de.interoberlin.merlot_android.model.mapping.functions.IFunctionDeserializer;

public class MappingController {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = MappingController.class.getSimpleName();

    // Model
    private Map<String, Mapping> existingMappings;
    private Map<String, Mapping> activeMappings;

    // Controller
    private DevicesController devicesController;

    private static MappingController instance;

    // </editor-fold>

    // --------------------
    // Constructors
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Constructors">

    private MappingController(Context context) {
        this.existingMappings = loadMappingsFromAssets(context);
        this.activeMappings = new HashMap<>();

        this.devicesController = DevicesController.getInstance();
    }

    public static MappingController getInstance(Context context) {
        if (instance == null) {
            instance = new MappingController(context);
        }

        return instance;
    }

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Loads all mapping files from assets and adds them to the list
     *
     * @param context context
     * @return list of mappings
     */
    private Map<String, Mapping> loadMappingsFromAssets(Context context) {
        Map<String, Mapping> mappings = new HashMap<>();

        try {
            for (String asset : Arrays.asList(context.getAssets().list(""))) {
                if (asset != null) {
                    if (asset.endsWith(context.getResources().getString(R.string.merlot_mapping_file_exception))) {
                        InputStream inputStream = context.getAssets().open(asset);

                        GsonBuilder gson = new GsonBuilder();
                        gson.registerTypeAdapter(IFunction.class, new IFunctionDeserializer());
                        gson.registerTypeAdapter(IAction.class, new IActionDeserializer());
                        Mapping m = gson.create().fromJson(IOUtils.toString(inputStream, "UTF-8"), Mapping.class);

                        if (m != null) {
                            Log.i(TAG, "Loaded mapping " + m.getName());
                            mappings.put(m.getName(), m);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return mappings;
    }

    public boolean activateMapping(Mapping.OnChangeListener ocListener, Mapping mapping) {
        Log.d(TAG, "Activate " + mapping.getName());

        if (existingMappings.containsKey(mapping.getName()))
            existingMappings.remove(mapping.getName());
        activeMappings.put(mapping.getName(), mapping);

        mapping.registerOnChangeListener(ocListener);

        flange(mapping);

        return true;
    }

    public boolean deactivateMapping(Mapping mapping) {
        Log.d(TAG, "Deactivate" + mapping.getName());

        if (activeMappings.containsKey(mapping.getName())) {
            activeMappings.remove(mapping.getName());
            existingMappings.put(mapping.getName(), mapping);
        }

        return true;
    }

    public void flangeAll() {

        for (Map.Entry<String, Mapping> e : activeMappings.entrySet()) {
            flange(e.getValue());
        }
    }

    public void flange(Mapping mapping) {
        Source source = mapping.getSource();
        Sink sink = mapping.getSink();

        BleDevice sourceDevice = devicesController.getAttachedDevices().get(source.getAddress());
        BleDevice sinkDevice = devicesController.getAttachedDevices().get(sink.getAddress());

        mapping.setSourceAttached(source != null && sourceDevice != null);
        mapping.setSinkAttached(sink != null && sinkDevice != null);

        if (sourceDevice != null) {
            if (mapping.isSourceAttached() && sourceDevice.getReadingObservable() != null) {
                mapping.subscribeTo(sourceDevice);
            } else {
                mapping.unsubscribeFrom(sourceDevice);
            }
        }
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public Map<String, Mapping> getExistingMappings() {
        return existingMappings;
    }

    public Map<String, Mapping> getActiveMappings() {
        return activeMappings;
    }

    public List<String> getExistingMappingNames() {
        List<String> mappingNames = new ArrayList<>();

        for (Map.Entry<String, Mapping> m : getExistingMappings().entrySet()) {
            mappingNames.add(m.getKey());
        }

        return mappingNames;
    }

    public Mapping getExistingMappingByName(String name) {
        return getExistingMappings().get(name);
    }

    /*
    public Mapping getActiveMappingByName(String name) {
        return getActiveMappings().get(name);
    }
    */

    public List<Mapping> getActiveMappingsAsList() {
        return new ArrayList<>(getActiveMappings().values());
    }

    // </editor-fold>
}
