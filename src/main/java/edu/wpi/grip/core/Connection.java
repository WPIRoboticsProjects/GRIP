package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;

import java.util.Set;

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
    public Connection(EventBus eventBus, OutputSocket<? extends T> outputSocket, InputSocket<T> inputSocket) throws InfiniteLoopException {
        this.eventBus = eventBus;
        this.outputSocket = outputSocket;
        this.inputSocket = inputSocket;

        checkNotNull(inputSocket);
        checkNotNull(outputSocket);
        checkNotNull(eventBus);

        if (willCreateInfiniteLoop(outputSocket, inputSocket)) {
            throw new InfiniteLoopException(outputSocket, inputSocket);
        }

        inputSocket.setValue(outputSocket.getValue());

        eventBus.register(this);
    }

    /**
     * Determines if this connection will create an infinite loop
     *
     * @param outputSocket The input socket to search for sockets
     * @param inputSocket  The output socket to search for sockets
     * @return False if this will create an infinite loop
     */
    private boolean willCreateInfiniteLoop(OutputSocket<?> outputSocket, InputSocket<?> inputSocket) {
        // There can't be any loops between two sockets that aren't owned by steps
        if (!outputSocket.getStep().isPresent()) return false;
        if (!inputSocket.getStep().isPresent()) return false;
        // If the input socket and the output socket are both on the same step this should not be a connection either
        if (outputSocket.getStep().equals(inputSocket.getStep())) return true;

        final Set<Step> inputStepsConnectedToOutput = outputSocket.getStep().get().getConnectedInputSteps();
        final Set<Step> outputStepsConnectedToInput = inputSocket.getStep().get().getConnectedOutputSteps();
        return inputStepsConnectedToOutput.stream().anyMatch(outStep -> outputStepsConnectedToInput.contains(outStep));
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
    public void removeConnection(StepRemovedEvent e) {
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

    @Subscribe
    public void removeConnection(SourceRemovedEvent e) {
        // Remove this connection if it's from a source that was removed
        for (OutputSocket socket : e.getSource().getOutputSockets()) {
            if (socket == this.outputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }
    }

    /**
     * Thrown when the user tries to create a connection that will result in an infinite loop.
     */
    public static class InfiniteLoopException extends Exception {
        public InfiniteLoopException(OutputSocket outputSocket, InputSocket<?> inputSocket) {
            super("Connecting " + outputSocket + " to " + inputSocket + " would create an infinite loop");
        }
    }
}
