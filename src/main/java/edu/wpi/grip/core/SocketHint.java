package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;

import java.util.Arrays;

/**
 * A <code>SocketHint</code> is a descriptor that gives some information about one of the inputs or outputs of an
 * Operation.  <code>SocketHint</code>s don't store actual values, and are merely used by steps and parts of the user
 * interface to learn how to talk to <code>Algorithm</code>s.
 */
public class SocketHint<T> {
    public enum View {NONE, SPINNER, SLIDER, RANGE, SELECT}

    private String identifier;
    private Class<T> type;
    private View view;
    private T[] domain;

    /**
     * The default value that the socket will hold if no other is specified.
     */
    private T defaultValue;


    /**
     * @param identifier   A user-presentable name for the socket, such as "Blur Radius".
     * @param type         The type of value held by the socket.
     * @param view         A hint at the type of GUI control to use to display the socket.
     * @param domain       A hint at the range of values that this socket can hold.  For numeric types, this can consist
     *                     of two elements that correspond to a minimum and maximum value.  The property does not make
     *                     sense for all types and is left unspecified for some.
     * @param defaultValue The value to use in place of the socket's value when none is specified.
     */
    public SocketHint(String identifier, Class<T> type, View view, T[] domain, T defaultValue) {
        this.identifier = identifier;
        this.type = type;
        this.view = view;
        this.domain = domain == null ? null : domain.clone();
        this.defaultValue = defaultValue;
    }

    public SocketHint(String identifier, Class<T> type, View view, T[] domain) {
        this(identifier, type, view, domain, (domain != null && domain.length > 0) ? domain[0] : null);
    }

    public SocketHint(String identifier, Class<T> type, View view) {
        this(identifier, type, view, null);
    }

    public SocketHint(String identifier, Class<T> type) {
        this(identifier, type, View.NONE);
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

    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("identifier", getIdentifier())
                .add("type", getType())
                .add("view", getView())
                .add("domain", Arrays.toString(getDomain()))
                .add("defaultValue", getDefaultValue())
                .toString();
    }
}
