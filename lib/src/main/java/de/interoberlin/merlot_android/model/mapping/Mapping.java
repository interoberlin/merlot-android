package de.interoberlin.merlot_android.model.mapping;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import de.interoberlin.merlot_android.model.IDisplayable;
import de.interoberlin.merlot_android.model.ble.BleDevice;
import de.interoberlin.merlot_android.model.mapping.actions.IAction;
import de.interoberlin.merlot_android.model.mapping.functions.IFunction;
import de.interoberlin.merlot_android.model.service.Reading;
import rx.Observer;
import rx.Subscription;

public class Mapping implements IDisplayable {
    // <editor-fold defaultstate="collapsed" desc="Members">

    public static final String TAG = Mapping.class.getSimpleName();

    private String name;
    private Integer debounce;
    private Source source;
    private Sink sink;
    private IFunction function;
    private IAction action;

    private transient boolean sourceAttached;
    private transient boolean sourceSubscribed;
    private transient boolean sinkAttached;
    private transient boolean triggered;

    private Subscription subscription;

    private OnChangeListener ocListener;

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
        Log.d(TAG, name + " subscribeTo " + device.getAddress() + " / " + device.getReadingObservable());

        sourceSubscribed = true;
        subscription = device.getReadingObservable()
                .debounce(debounce, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onNext(Reading reading) {
                        Log.v(TAG, "onNext / reading " + reading.meaning + " : " + reading.value);

                        if (function.isTriggered(Float.parseFloat(reading.value.toString()))) {
                            Log.d(TAG, "triggered");
                            setTriggered(true);
                            action.perform(sink);
                        } else {
                            setTriggered(false);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.getMessage());
                    }
                });
    }

    /**
     * Unsubscribes from a device
     *
     * @param device device
     */
    public void unsubscribeFrom(BleDevice device) {
        Log.d(TAG, name + " unsubscribeFrom " + device.getAddress() + " / " + device.getReadingObservable());

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
