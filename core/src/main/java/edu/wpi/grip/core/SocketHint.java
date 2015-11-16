package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * A <code>SocketHint</code> is a descriptor that gives some information about one of the inputs or outputs of an
 * Operation.  <code>SocketHint</code>s don't store actual values, and are merely used by steps and parts of the user
 * interface to learn how to talk to <code>Algorithm</code>s.
 */
public class SocketHint<T> {
    public enum View {NONE, SPINNER, SLIDER, RANGE, SELECT, CHECKBOX}

    private final String identifier;
    private final Class<T> type;
    private final Supplier<T> initialValueSupplier;
    private final View view;
    private final T[] domain;
    private boolean publishable;

    /**
     * @param identifier           A user-presentable name for the socket, such as "Blur Radius".
     * @param type                 The type of value held by the socket.
     * @param view                 A hint at the type of GUI control to use to display the socket.
     * @param domain               A hint at the range of values that this socket can hold.  For numeric types, this
     *                             can consist of two elements that correspond to a minimum and maximum value.  The
     *                             property does not make sense for all types and is left unspecified for some.
     * @param initialValueSupplier A function that returns an initial value for newly-created sockets.
     * @param publishable          If this isn't true, the socket won't be able to be set as published, and the GUI
     *                             should also not show a button to publish it.  This is false by default.
     */
    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view, T[] domain,
                      boolean publishable) {
        this.identifier = identifier;
        this.type = type;
        this.initialValueSupplier = initialValueSupplier;
        this.view = view;
        this.domain = domain == null ? null : domain.clone();
        this.publishable = publishable;
    }

    public SocketHint(String identifier, Class<T> type, T initialValue, View view, T[] domain, boolean publishable) {
        this(identifier, type, () -> initialValue, view, domain, publishable);
    }

    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view, T[] domain) {
        this(identifier, type, initialValueSupplier, view, domain, false);
    }

    public SocketHint(String identifier, Class<T> type, T initialValue, View view, T[] domain) {
        this(identifier, type, () -> initialValue, view, domain);
    }

    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view) {
        this(identifier, type, initialValueSupplier, view, null);
    }

    public SocketHint(String identifier, Class<T> type, T initialValue, View view) {
        this(identifier, type, () -> initialValue, view);
    }

    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier) {
        this(identifier, type, initialValueSupplier, View.NONE);
    }

    public SocketHint(String identifier, Class<T> type, T initialValue) {
        this(identifier, type, () -> initialValue);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getType() {
        return type;
    }

    public View getView() {
        return view;
    }

    public T[] getDomain() {
        return domain;
    }

    public boolean isPublishable() {
        return this.publishable;
    }

    public T createInitialValue() {
        return initialValueSupplier.get();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("identifier", getIdentifier())
                .add("type", getType())
                .add("view", getView())
                .add("domain", Arrays.toString(getDomain()))
                .toString();
    }
}
