package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connection is a rule that causes one socket to update to always the value of another socket.
 */
public class Connection<T> {
    private final EventBus eventBus;
    private final OutputSocket<? extends T> outputSocket;
    private final InputSocket<T> inputSocket;

    /**
     * @param eventBus     The Guava {@link EventBus} used by the application.
     * @param outputSocket The socket to listen for changes in.
     * @param inputSocket  A different socket to update when a change occurs in the first.
     */
    public Connection(EventBus eventBus, OutputSocket<? extends T> outputSocket, InputSocket<T> inputSocket) {
        this.eventBus = eventBus;
        this.outputSocket = outputSocket;
        this.inputSocket = inputSocket;

        checkNotNull(inputSocket);
        checkNotNull(outputSocket);
        checkNotNull(eventBus);

        inputSocket.setValue(outputSocket.getValue());

        eventBus.register(this);
    }

    public OutputSocket<? extends T> getOutputSocket() {
        return this.outputSocket;
    }

    public InputSocket<T> getInputSocket() {
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
