package edu.wpi.grip.core.sockets;


import edu.wpi.grip.core.Connection;

import com.fasterxml.uuid.NoArgGenerator;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Provider;

/**
 * Concrete implementation of the {@link InputSocket}.
 *
 * @param <T> The type of the value that this socket stores
 */
@XStreamAlias("grip:Input")
public class InputSocketImpl<T> extends SocketImpl<T> implements InputSocket<T> {
  private final AtomicBoolean dirty = new AtomicBoolean(false);

  /**
   * @param eventBus   The Guava {@link EventBus} used by the application.
   * @param socketHint {@link #getSocketHint}
   * @param uuid       A UUID for this object.
   */
  InputSocketImpl(Provider<EventBus> eventBus, SocketHint<T> socketHint, UUID uuid) {
    super(eventBus, socketHint, Socket.Direction.INPUT, uuid);
  }

  /**
   * Reset the socket to its default value when it's no longer connected to anything.  This prevents
   * removed connections from continuing to have an effect on steps because they still hold
   * references to the values they were connected to.
   */
  @Override
  public void removeConnection(Connection connection) {
    super.removeConnection(connection);
    if (this.getConnections().isEmpty()) {
      this.setValue(this.getSocketHint().createInitialValue().orElse(null));
    }
  }

  @Override
  public boolean dirtied() {
    return dirty.compareAndSet(true, false);
  }

  @Override
  public void onValueChanged() {
    dirty.set(true);
  }

  /**
   * Needed to get around Guice's inability to inject a generic typed factory.
   */
  @Singleton
  public static class FactoryImpl implements Factory {
    private final Provider<EventBus> eventBus;
    private final NoArgGenerator uuidGenerator;

    @Inject
    FactoryImpl(Provider<EventBus> eventBus, NoArgGenerator uuidGenerator) {
      this.eventBus = eventBus;
      this.uuidGenerator = uuidGenerator;
    }

    @Override
    public <T> InputSocket<T> create(SocketHint<T> hint) {
      return new InputSocketImpl<>(eventBus, hint, uuidGenerator.generate());
    }
  }


}
