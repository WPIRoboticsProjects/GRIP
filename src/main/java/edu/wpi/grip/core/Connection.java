package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connection is a rule that causes one socket to update to always the value of another socket.
 */
public class Connection<T> {
    private final EventBus eventBus;
    private final Socket<? extends T> outputSocket;
    private final Socket<T> inputSocket;

    /**
     * @param eventBus     The Guava {@link EventBus} used by the application.
     * @param outputSocket The socket to listen for changes in.
     * @param inputSocket  A different socket to update when a change occurs in the first.
     */
    public Connection(EventBus eventBus, Socket<? extends T> outputSocket, Socket<T> inputSocket) {
        this.eventBus = eventBus;
        this.outputSocket = outputSocket;
        this.inputSocket = inputSocket;

        checkNotNull(inputSocket);
        checkNotNull(outputSocket);
        checkNotNull(eventBus);
        checkArgument(!Socket.Direction.INPUT.equals(outputSocket.getDirection()),
                "outputSocket cannot be an input socket");
        checkArgument(!Socket.Direction.OUTPUT.equals(inputSocket.getDirection()),
                "inputSocket cannot be an output socket");

        if (inputSocket == outputSocket) {
            throw new IllegalArgumentException("inputSocket cannot be the same as outputSocket");
        }

        inputSocket.setValue(outputSocket.getValue());

        eventBus.register(this);
    }

    public Socket<? extends T> getOutputSocket() {
        return this.outputSocket;
    }

    public Socket<T> getInputSocket() {
        return this.inputSocket;
    }

    @Subscribe
    public void onOutputChanged(SocketChangedEvent e) {
        if (e.getSocket() == outputSocket) {
            inputSocket.setValue(outputSocket.getValue());
        }
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent e) {
        // Remove this connection if one of the steps it was connected to was removed
        for (Socket socket : e.getStep().getOutputSockets()) {
            if (socket == this.inputSocket || socket == this.outputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }

        for (Socket socket : e.getStep().getInputSockets()) {
            if (socket == this.inputSocket || socket == this.outputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }
    }
}
