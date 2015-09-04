package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SocketChangedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connection is a rule that causes one socket to update to always the value of another socket.
 */
public class Connection<T> {
    final private Socket<? extends T> outputSocket;
    final private Socket<T> inputSocket;

    /**
     * @param eventBus     The Guava {@link EventBus} used by the application.
     * @param outputSocket The socket to listen for changes in.
     * @param inputSocket  A different socket to update when a change occurs in the first.
     */
    public Connection(EventBus eventBus, Socket<? extends T> outputSocket, Socket<T> inputSocket) {
        this.outputSocket = outputSocket;
        this.inputSocket = inputSocket;

        checkNotNull(inputSocket);
        checkNotNull(outputSocket);
        checkNotNull(eventBus);

        if (inputSocket.equals(outputSocket)) {
            throw new IllegalArgumentException("inputSocket cannot be the same as outputSocket");
        }

        eventBus.register(this);
    }

    @Subscribe
    public void onOutputChanged(SocketChangedEvent event) {
        if (event.getSocket() == outputSocket) {
            inputSocket.setValue(outputSocket.getValue());
        }
    }
}
