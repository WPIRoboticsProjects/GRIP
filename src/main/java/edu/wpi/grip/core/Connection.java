package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.SourceRemovedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connection is a rule that causes one socket to update to always the value of another socket.
 */
@XStreamAlias(value = "grip:Connection")
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
        checkArgument(Connection.canConnect(outputSocket, inputSocket), "Cannot connect sockets");

        inputSocket.setValue(outputSocket.getValue());

        eventBus.register(this);
    }

    /**
     * @return true if a connection can be made from the given output socket to the given input socket
     */
    @SuppressWarnings("unchecked")
    public static boolean canConnect(Socket socket1, Socket socket2) {
        final OutputSocket<?> outputSocket;
        final InputSocket<?> inputSocket;

        // One socket must be an input and one must be an output
        if (socket1.getDirection() == socket2.getDirection()) {
            return false;
        }

        if (socket1.getDirection().equals(Socket.Direction.OUTPUT)) {
            outputSocket = (OutputSocket) socket1;
            inputSocket = (InputSocket) socket2;
        } else {
            inputSocket = (InputSocket) socket1;
            outputSocket = (OutputSocket) socket2;
        }

        final SocketHint outputHint = socket1.getSocketHint();
        final SocketHint inputHint = socket2.getSocketHint();

        // The input socket must be able to hold the type of value that the output socket contains
        if (!inputHint.getType().isAssignableFrom(outputHint.getType())) {
            return false;
        }

        // Input sockets can only be connected to one thing
        if (!inputSocket.getConnections().isEmpty()) {
            return false;
        }

        // If both sockets are in steps, the output must be before the input in the pipeline.  This prevents "backwards"
        // connections, which both enforces a well-organized pipeline and prevents feedback loops.
        final boolean[] backwards = {false};
        outputSocket.getStep().ifPresent(outputStep -> inputSocket.getStep().ifPresent(inputStep -> {
            final Pipeline pipeline = checkNotNull(inputStep.getPipeline(), "Pipeline is null");
            if (!pipeline.isBefore(outputStep, inputStep)) {
                backwards[0] = true;
            }
        }));

        return !backwards[0];


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
}
