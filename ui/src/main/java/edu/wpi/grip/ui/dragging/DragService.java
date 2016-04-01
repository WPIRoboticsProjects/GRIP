package edu.wpi.grip.ui.dragging;


import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

/**
 * A service to provide data transfer capabilities between two controllers.
 * Concrete versions of the service are usually {@link com.google.inject.Singleton Singletons}
 * so that they can be injected into the two controllers.
 *
 * @param <T> The value that the object property holds.
 */
public abstract class DragService<T> {
    private final ObjectProperty<T> dragProperty;

    /**
     * @param name The name for the {@link SimpleObjectProperty}
     */
    public DragService(String name) {
        this.dragProperty = new SimpleObjectProperty<>(this, name);
    }

    /**
     * @return The read only version of this object property.
     */
    public ReadOnlyObjectProperty<T> getDragProperty() {
        return dragProperty;
    }

    /**
     * @return The value stored in the object property.
     */
    public Optional<T> getValue() {
        return Optional.ofNullable(dragProperty.get());
    }

    /**
     * Begins the drag action
     *
     * @param value The value to be transferred during the drag.
     */
    public void beginDrag(T value) {
        this.dragProperty.set(value);
    }

    /**
     * This should be called when the drag action is complete.
     */
    public void completeDrag() {
        dragProperty.setValue(null);
    }
}
