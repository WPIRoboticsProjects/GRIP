package edu.wpi.grip.core;


import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.events.SocketPublishedEvent;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Represents the output of an {@link Operation}. The OutputSocket also provides the ability to
 * make the value in a socket published.
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
     * Indicates if this socket is published or not.
     */
    private boolean published = false;

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint See {@link #getSocketHint()}
     * @param value      See {@link #getValue()}
     */
    public OutputSocket(EventBus eventBus, SocketHint<T> socketHint, T value) {
        super(eventBus, socketHint, value, Direction.OUTPUT);
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    public OutputSocket(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.OUTPUT);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);

        // If the socket's value is set to be published, also send a SocketPublishedEvent to notify any sinks that
        // it has changed.
        if (this.isPublished()) {
            eventBus.post(new SocketPublishedEvent(this));
        }
    }

    /**
     * @param published If <code>true</code>, this socket will be published by any sink that is currently active.  For
     *                  example, it may be set as a NetworkTables value.  The socket must be publishable.
     * @see SocketHint#SocketHint(String, Class, Supplier, SocketHint.View, Object[], boolean)
     */
    public void setPublished(boolean published) {
        checkArgument(this.getSocketHint().isPublishable() || !published, "socket is not publishable");

        /* Check if this value is changing */
        final boolean change = isPublished() != published;
        /* Save whether it was published before. */
        final boolean wasPublished = isPublished();
        /* Set this now so that it is published when the event handlers run */
        this.published = published;
        /*
         * If the socket wasn't previously published and is now, send a SocketPublishedEvent to publish an initial
         * value.
         */
        if (!wasPublished && change) {
            eventBus.post(new SocketPublishedEvent(this));
        }
    }

    /**
     * @return Whether or not this socket should be published.
     * @see #setPublished(boolean)
     */
    public boolean isPublished() {
        return this.published;
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


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socketHint", getSocketHint())
                .add("value", getValue())
                .add("previewed", isPreviewed())
                .add("published", isPublished())
                .add("direction", getDirection())
                .toString();
    }
}
