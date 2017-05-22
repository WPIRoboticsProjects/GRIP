package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.events.SocketPreviewChangedEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A concrete implementation of the {@link OutputSocket}.
 *
 * @param <T> The type that that this socket holds.
 */
@XStreamAlias("grip:Output")
public class OutputSocketImpl<T> extends SocketImpl<T> implements OutputSocket<T> {
  private final EventBus eventBus;
  /**
   * Indicates if the socket is being previewed.
   */
  private boolean previewed = false;

  @VisibleForTesting
  OutputSocketImpl(EventBus eventBus, SocketHint<T> socketHint) {
    this(eventBus, socketHint, socketHint.getIdentifier());
  }

  /**
   * @param eventBus   The Guava {@link EventBus} used by the application.
   * @param socketHint {@link #getSocketHint}
   * @param uid        a unique string for identifying this socket
   */
  OutputSocketImpl(EventBus eventBus, SocketHint<T> socketHint, String uid) {
    super(eventBus, socketHint, Direction.OUTPUT, uid);
    this.eventBus = eventBus;
  }

  @Override
  public boolean isPreviewed() {
    return this.previewed;
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

  public static class FactoryImpl implements OutputSocket.Factory {
    private final EventBus eventBus;

    @Inject
    FactoryImpl(EventBus eventBus) {
      this.eventBus = eventBus;
    }

    @Override
    public <T> OutputSocket<T> create(SocketHint<T> hint, String uid) {
      return new OutputSocketImpl<>(eventBus, hint, uid);
    }
  }
}
