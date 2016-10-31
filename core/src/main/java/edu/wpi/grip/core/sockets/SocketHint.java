package edu.wpi.grip.core.sockets;

import com.google.common.base.MoreObjects;

import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * A <code>SocketHint</code> is a descriptor that gives some information about one of the inputs or
 * outputs of an Operation.  <code>SocketHint</code>s don't store actual values, and are merely used
 * by steps and parts of the user interface to learn how to talk to <code>Algorithm</code>s.
 */
public interface SocketHint<T> {

  /**
   * The name for this socket.
   */
  String getIdentifier();

  Class<T> getType();

  /**
   * Determines if this is able to contain the type of value that the other socket hint contains.
   *
   * @param other The other socket hint to check if this hint's type can contain it
   * @return True the two can be connected together
   */
  boolean isCompatibleWith(SocketHint other);

  /**
   * The type of view that this hint reccomends being displayed with.
   */
  View getView();

  /**
   * A hint at the range of values that this socket can hold. For numeric types, this can consist of
   * two elements that correspond.
   */
  Optional<T[]> getDomain();

  /**
   * A user-presentable string to represent the type of this socket.  This may be empty.
   */
  String getTypeLabel();

  /**
   * Constructs the initial value if an initial value provider was supplied.
   *
   * @return Optionally, the initial value for the socket
   */
  Optional<T> createInitialValue();

  enum View {
    NONE, TEXT, SLIDER, RANGE, SELECT, CHECKBOX
  }

  /**
   * A concrete implementation of the SocketHint class that provides the functionality of a raw
   * SocketHint.
   */
  final class BasicSocketHint<T> implements SocketHint<T> {
    private final String identifier;
    private final Class<T> type;
    private final Optional<Supplier<T>> initialValueSupplier;
    private final View view;
    private final Optional<T[]> domain;

    /**
     * @param type                 The type of value held by the socket.
     * @param identifier           A user-presentable name for the socket, such as "Blur Radius".
     * @param initialValueSupplier A function that returns an initial value for newly-created
     *                             sockets.
     * @param view                 A hint at the type of GUI control to use to display the socket.
     * @param domain               A hint at the range of values that this socket can hold.  For
     *                             numeric types, this can consist of two elements that correspond
     *                             to a minimum and maximum value.  The property does not make sense
     *                             for all types and is left unspecified for some
     */
    private BasicSocketHint(Class<T> type, String identifier, Optional<Supplier<T>>
        initialValueSupplier, View view, Optional<T[]> domain) {
      this.type = type;
      this.identifier = identifier;
      this.initialValueSupplier = initialValueSupplier;
      this.view = view;
      this.domain = domain;
    }

    @Override
    public String getIdentifier() {
      return identifier;
    }

    @Override
    public Class<T> getType() {
      return type;
    }

    @Override
    public View getView() {
      return view;
    }

    @Override
    public Optional<T[]> getDomain() {
      return domain;
    }

    @Override
    public String getTypeLabel() {
      if (type.getAnnotation(NoSocketTypeLabel.class) != null || type.isEnum()
          || type.equals(List.class)) {
        // Enums labels are kind of redundant, and Lists actually represent ranges
        return "";
      } else if (Mat.class.equals(type)) {
        // "Mats" represent images
        return "Image";
      } else if (CascadeClassifier.class.equals(type)) {
        // "CascadeClassifier" is too long and the name is already on the operation
        return "Classifier";
      } else {
        // For any other type, just use the name of the class
        return type.getSimpleName();
      }
    }

    @Override
    public Optional<T> createInitialValue() {
      if (initialValueSupplier.isPresent()) {
        return Optional.ofNullable(initialValueSupplier.get().get());
      }
      return Optional.empty();
    }

    @Override
    public boolean isCompatibleWith(SocketHint other) {
      return getType().isAssignableFrom(other.getType());
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
  }

  /**
   * A SocketHintDecorator that easily redecorates the {@link #getIdentifier()} method.
   *
   * @param <T> The type of the SocketHint
   */
  final class IdentiferOverridingSocketHintDecorator<T> extends SocketHintDecorator<T> {
    private final String identifier;

    public IdentiferOverridingSocketHintDecorator(SocketHint<T> decorated, String identifier) {
      super(decorated);
      this.identifier = checkNotNull(identifier, "identifier cannot be null");
    }

    @Override
    public String getIdentifier() {
      return identifier;
    }
  }

  abstract class SocketHintDecorator<T> implements SocketHint<T> {
    private final SocketHint<T> decorated;

    public SocketHintDecorator(SocketHint<T> decorated) {
      this.decorated = checkNotNull(decorated, "Decorated SocketHint cannot be null");
    }

    protected SocketHint<T> getDecorated() {
      return decorated;
    }

    @Override
    public String getIdentifier() {
      return decorated.getIdentifier();
    }

    @Override
    public Class<T> getType() {
      return decorated.getType();
    }

    @Override
    public boolean isCompatibleWith(SocketHint other) {
      return decorated.isCompatibleWith(other);
    }

    @Override
    public View getView() {
      return decorated.getView();
    }

    @Override
    public Optional<T[]> getDomain() {
      return decorated.getDomain();
    }

    @Override
    public String getTypeLabel() {
      return decorated.getTypeLabel();
    }

    @Override
    public Optional<T> createInitialValue() {
      return decorated.createInitialValue();
    }
  }

  class Builder<T> {
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

    public Builder<T> initialValue(@Nullable T value) {
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
        initialValueSupplier.orElseThrow(() -> new NoSuchElementException("A View other than "
            + "`NONE` was supplied but not an initial value"));
      }
      return new BasicSocketHint<>(
          this.type,
          identifier.orElseThrow(() -> new NoSuchElementException("The identifier was not "
              + "supplied")),
          initialValueSupplier,
          view,
          domain
      );
    }
  }
}
