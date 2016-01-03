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

    /**
     * @param type                 The type of value held by the socket.
     * @param identifier           A user-presentable name for the socket, such as "Blur Radius".
     * @param initialValueSupplier A function that returns an initial value for newly-created sockets.
     * @param view                 A hint at the type of GUI control to use to display the socket.
     * @param domain               A hint at the range of values that this socket can hold.  For numeric types, this
     *                             can consist of two elements that correspond to a minimum and maximum value.  The
     *                             property does not make sense for all types and is left unspecified for some
     */
    private SocketHint(Class<T> type, String identifier, Optional<Supplier<T>> initialValueSupplier, View view, Optional<T[]> domain) {
        this.type = type;
        this.identifier = identifier;
        this.initialValueSupplier = initialValueSupplier;
        this.view = view;
        this.domain = domain;
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

        public Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> identifier(String identifier) {
            this.identifier = Optional.of(identifier);
            return this;
        }

        public Builder<T> initialValueSupplier(Supplier<T> initialValueSupplier) {
            this.initialValueSupplier = Optional.of(initialValueSupplier);
            return this;
        }

        public Builder<T> initialValue(T value) {
            this.initialValueSupplier = value == null ? Optional.empty() : Optional.of(() -> value);
            return this;
        }

        public Builder<T> view(View view) {
            this.view = checkNotNull(view, "The view can not be null");
            return this;
        }

        public Builder<T> domain(T[] domain) {
            this.domain = Optional.of(domain);
            return this;
        }

        public SocketHint<T> build() throws NoSuchElementException {
            if (!view.equals(View.NONE)) {
                initialValueSupplier.orElseThrow(() -> new NoSuchElementException("A View other than `NONE` was supplied but not an initial value"));
            }
            return new SocketHint<>(
                    this.type,
                    identifier.orElseThrow(() -> new NoSuchElementException("The identifier was not supplied")),
                    initialValueSupplier,
                    view,
                    domain
            );
        }
    }
}
