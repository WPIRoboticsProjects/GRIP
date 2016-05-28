package edu.wpi.grip.core.sockets;


import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import edu.wpi.grip.core.events.SocketPreviewChangedEvent;

/**
 * A concrete implementation of the {@link OutputSocket}
 *
 * @param <T> The type that that this socket holds.
 */
@XStreamAlias("grip:Output")
public class OutputSocketImpl<T> extends SocketImpl<T> implements OutputSocket<T> {
    private final EventBus eventBus;
    /**
     * Indicates if the socket is being previewed
     */
    private boolean previewed = false;

    public static class FactoryImpl implements OutputSocket.Factory {
        private final EventBus eventBus;

        @Inject
        FactoryImpl(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        @Override
        public <T> OutputSocket<T> create(SocketHint<T> hint) {
            return new OutputSocketImpl<>(eventBus, hint);
        }
    }

    /**
     * @param eventBus   The Guava {@link EventBus} used by the application.
     * @param socketHint {@link #getSocketHint}
     */
    OutputSocketImpl(EventBus eventBus, SocketHint<T> socketHint) {
        super(eventBus, socketHint, Direction.OUTPUT);
        this.eventBus = eventBus;
    }



    @Override
    public void setPreviewed(boolean previewed) {
        boolean changed = previewed != this.previewed;
        this.previewed = previewed;

        // Only send an event if the field was actually changed
        if (changed) {
            eventBus.post(new SocketPreviewChangedEvent(this));
        }
    }

    @Override
    public boolean isPreviewed() {
        return this.previewed;
    }

    @Override
    public void resetValueToInitial() {
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
