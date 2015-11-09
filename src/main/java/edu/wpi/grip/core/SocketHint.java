package edu.wpi.grip.core;

import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A <code>SocketHint</code> is a descriptor that gives some information about one of the inputs or outputs of an
 * Operation.  <code>SocketHint</code>s don't store actual values, and are merely used by steps and parts of the user
 * interface to learn how to talk to <code>Algorithm</code>s.
 */
public class SocketHint<T> {
    public enum View {NONE, SPINNER, SLIDER, RANGE, SELECT, CHECKBOX}

    private final String identifier;
    private final Class<T> type;
    private final Optional<Supplier<T>> initialValueSupplier;
    private final View view;
    private final Optional<T[]> domain;
    private final boolean publishable;

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
    @Deprecated
    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view, T[] domain,
                      boolean publishable) {
        this.identifier = identifier;
        this.type = type;
        this.initialValueSupplier = Optional.of(initialValueSupplier);
        this.view = view;
        this.domain = domain == null ? Optional.empty() : Optional.of(domain.clone());
        this.publishable = publishable;
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, T initialValue, View view, T[] domain, boolean publishable) {
        this(identifier, type, () -> initialValue, view, domain, publishable);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view, T[] domain) {
        this(identifier, type, initialValueSupplier, view, domain, false);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, T initialValue, View view, T[] domain) {
        this(identifier, type, () -> initialValue, view, domain);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier, View view) {
        this(identifier, type, initialValueSupplier, view, null);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, T initialValue, View view) {
        this(identifier, type, () -> initialValue, view);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, Supplier<T> initialValueSupplier) {
        this(identifier, type, initialValueSupplier, View.NONE);
    }

    @Deprecated
    public SocketHint(String identifier, Class<T> type, T initialValue) {
        this(identifier, type, () -> initialValue);
    }

    private SocketHint(Class<T> type, String identifier, Optional<Supplier<T>> initialValueSupplier, View view, Optional<T[]> domain, boolean publishable) {
        this.type = type;
        this.identifier = identifier;
        this.initialValueSupplier = initialValueSupplier;
        this.view = view;
        this.domain = domain;
        this.publishable = publishable;
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

    public Optional<T[]> getDomain() {
        return domain;
    }

    public boolean isPublishable() {
        return this.publishable;
    }

    public Optional<T> createInitialValue() {
        if (initialValueSupplier.isPresent()) {
            return Optional.ofNullable(initialValueSupplier.get().get());
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("identifier", getIdentifier())
                .add("type", getType())
                .add("view", getView())
                .add("domain", Arrays.toString(getDomain().orElse(null)))
                .toString();
    }

    public static class Builder<T> {
        private final Class<T> type;
        private Optional<String> identifier = Optional.empty();
        private Optional<Supplier<T>> initialValueSupplier = Optional.empty();
        private View view = View.NONE;
        private Optional<T[]> domain = Optional.empty();
        private Boolean publishable = false;

        public Builder(Class<T> type) {
            this.type = type;
        }

        public Builder identifier(String identifier) {
            this.identifier = Optional.of(identifier);
            return this;
        }

        public Builder initialValueSupplier(Supplier<T> initialValueSupplier) {
            this.initialValueSupplier = Optional.of(initialValueSupplier);
            return this;
        }

        public Builder initialValue(T value) {
            this.initialValueSupplier = Optional.of(() -> value);
            return this;
        }

        public Builder view(View view) {
            this.view = checkNotNull(view, "The view can not be null");
            return this;
        }

        public Builder domain(T[] domain) {
            this.domain = Optional.of(domain);
            return this;
        }

        public Builder publishable(boolean publishable) {
            this.publishable = publishable;
            return this;
        }

        public SocketHint<T> build() throws NoSuchElementException {
            identifier.orElseThrow(() -> new NoSuchElementException("The identifier was not supplied"));
            return new SocketHint<>(this.type, identifier.get(), initialValueSupplier, view, domain, publishable);
        }
    }
}
