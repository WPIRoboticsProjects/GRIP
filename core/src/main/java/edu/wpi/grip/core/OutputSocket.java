package edu.wpi.grip.core;


import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Represents the output of an {@link Operation}.
 *
 * @param <T> The type of the value that this socket stores.
 */
@XStreamAlias(value = "grip:Output")
public class OutputSocket<T> extends Socket<T> {

    /**
     * Indicates if the socket is being previewed
     */
    private boolean previewed = false;

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public OutputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.OUTPUT);
        getValue().orElseThrow(()-> new NoSuchElementException("The SocketHint for an output socket must have an initial value to be valid"));
    }

    @Override
    public void setValueOptional(Optional<? extends T> optionalValue) {
        super.setValueOptional(optionalValue);
    }

    @Override
    public void setValue(@Nullable T value) {
        super.setValue(value);
    }

    /**
     * @param previewed If <code>true</code>, this socket will be shown in a preview in the GUI.
     */
    public void setPreviewed(boolean previewed) {
        boolean changed = previewed != this.previewed;
        this.previewed = previewed;

        // Only send an event if the field was actually changed
        if (changed) {
            eventBus.post(new SocketPreviewChangedEvent(this));
        }
    }

    /**
     * @return Whether or not this socket is shown in a preview in the GUI
     * @see #setPreviewed(boolean) d(boolean)
     */
    public boolean isPreviewed() {
        return this.previewed;
    }

    protected void resetValueToInitial() {
        this.setValue(this.getSocketHint()
                .createInitialValue()
                .orElse(null));
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socketHint", getSocketHint())
                .add("value", getValue())
                .add("previewed", isPreviewed())
                .add("direction", getDirection())
                .toString();
    }
}
