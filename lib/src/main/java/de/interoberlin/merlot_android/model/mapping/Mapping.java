package de.interoberlin.merlot_android.model.mapping;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.interoberlin.merlot_android.model.IDisplayable;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.mapping.actions.IAction;
import de.interoberlin.merlot_android.model.mapping.exceptions.MappingException;
import de.interoberlin.merlot_android.model.mapping.functions.IFunction;
import de.interoberlin.merlot_android.model.service.Reading;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import rx.Observer;
import rx.Subscription;

public class Mapping extends RealmObject implements IDisplayable {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = Mapping.class.getSimpleName();

    @PrimaryKey private String name;
    private Integer debounce;
    private Source source;
    private Sink sink;
    private IFunction function;
    private IAction action;

    @Ignore private transient boolean sourceAttached;
    @Ignore private transient boolean sourceSubscribed;
    @Ignore private transient boolean sinkAttached;
    @Ignore private transient boolean triggered;

    @Ignore private Subscription subscription;

    @Ignore private OnChangeListener ocListener;

    // </editor-fold>

    // --------------------
    // Methods
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Methods">

    /**
     * Subscribes to a device
     *
     * @param device device
     */
    public void subscribeTo(BleDevice device) {
        Log.d(TAG, "Mapping " + name + " subscribes to " + device.getAddress() + " / " + device.getReadingObservable());

        sourceSubscribed = true;
        subscription = device.getReadingObservable()
                .debounce(debounce, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onNext(Reading reading) {
                        float value = 0;
                        if (getSource().getNode() == null) {
                            value = Float.parseFloat(reading.value.toString());
                        } else {
                            try {
                                value = Float.parseFloat(getNodeValue(reading.value.toString(), source.getNode()));
                            } catch (MappingException e) {
                                e.printStackTrace();
                            }
                        }

                        if (function.isTriggered(value)) {
                            Log.d(TAG, "Mapping triggered");
                            setTriggered(true);
                            action.perform(sink);
                        } else {
                            setTriggered(false);
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    /**
     * Retrieves a node value from an unknown json
     * <p/>
     * E.g.
     * json  = { "coordinate" : { "x" : 1.0, "y" : 2.0 }}
     * nodes = "coordinate.x"
     * will return 1.0
     *
     * @param json  json file
     * @param nodes period-concatenated nodes
     * @return node value
     */
    private String getNodeValue(String json, String nodes) throws MappingException {
        LinkedTreeMap ltm = (LinkedTreeMap) new GsonBuilder().create().fromJson(json, Object.class);
        List<String> nodeArray = Arrays.asList(nodes.split("\\."));

        for (int i=0; i<nodeArray.size(); i++) {
            String node = nodeArray.get(i);

            if (i == nodeArray.size()-1) {
                // Leaf reached
                return ltm.get(node).toString();
            } else if (ltm.containsKey(node)) {
                // Dig deeper
                ltm = (LinkedTreeMap) new GsonBuilder().create().fromJson(ltm.get(node).toString(), Object.class);
            } else {
                throw new MappingException("Unable to get node " + nodes + " from json " + json);
            }
        }

        return ltm.toString();
    }

    /**
     * Unsubscribes from a device
     *
     * @param device device
     */
    public void unsubscribeFrom(BleDevice device) {
        Log.d(TAG, "Mapping " + name + " unsubscribes from " + device.getAddress() + " / " + device.getReadingObservable());

        sourceSubscribed = false;
        if (subscription != null)
            subscription.unsubscribe();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(this.getName()).append(", \n");
        sb.append("source=").append(this.getSource().toString()).append(", \n");
        sb.append("sink=").append(this.getSink().toString()).append(", \n");
        sb.append("function=").append(this.getFunction().toString()).append(", \n");
        sb.append("action=").append(this.getAction().toString());

        return sb.toString();
    }

    // </editor-fold>

    // --------------------
    // Getters / Setters
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Getters / Setters">

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDebouce() {
        return debounce;
    }

    public void setDebounce(Integer debounce) {
        this.debounce = debounce;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public IFunction getFunction() {
        return function;
    }

    public void setFunction(IFunction function) {
        this.function = function;
    }

    public IAction getAction() {
        return action;
    }

    public void setAction(IAction action) {
        this.action = action;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void setSourceAttached(boolean sourceAttached) {
        this.sourceAttached = sourceAttached;
    }

    public boolean isSinkAttached() {
        return sinkAttached;
    }

    public boolean isSourceSubscribed() {
        return sourceSubscribed;
    }

    public void setSourceSubscribed(boolean sourceSubscribed) {
        this.sourceSubscribed = sourceSubscribed;
    }

    public void setSinkAttached(boolean sinkAttached) {
        this.sinkAttached = sinkAttached;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
    }

    public boolean isSourceAttached() {
        return sourceAttached;
    }

    // </editor-fold>

    // --------------------
    // Callback interfaces
    // --------------------

    // <editor-fold defaultstate="collapsed" desc="Callback interfaces">

    public interface OnChangeListener {
        void onChange(Mapping mapping);
    }

    public void registerOnChangeListener(OnChangeListener ocListener) {
        this.ocListener = ocListener;
    }

    // </editor-fold>
}
