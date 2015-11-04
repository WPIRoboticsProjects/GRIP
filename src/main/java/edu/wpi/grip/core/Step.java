package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SocketChangedEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A step is an instance of an operation in a pipeline.  A step contains a list of input and output sockets, and it
 * runs the operation whenever one of the input sockets changes.
 */
public class Step {
    private Operation operation;
    private InputSocket<?>[] inputSockets;
    private OutputSocket<?>[] outputSockets;

    /**
     * @param eventBus  The Guava {@link EventBus} used by the application.
     * @param operation The operation that is performed at this step.
     */
    public Step(EventBus eventBus, Operation operation) {
        this.operation = operation;

        checkNotNull(eventBus);
        checkNotNull(operation);

        // Create the list of input and output sockets, and mark this step as their owner.
        inputSockets = operation.createInputSockets(eventBus);
        for (Socket<?> socket : inputSockets) {
            socket.setStep(Optional.of(this));
        }

        outputSockets = operation.createOutputSockets(eventBus);
        for (Socket<?> socket : outputSockets) {
            socket.setStep(Optional.of(this));
        }

        operation.perform(inputSockets, outputSockets);

        eventBus.register(this);
    }

    /**
     * @return The underlying <code>Operation</code> that this step performs
     */
    public Operation getOperation() {
        return this.operation;
    }

    /**
     * @return An array of <code>Socket</code>s that hold the inputs to this step
     */
    public InputSocket<?>[] getInputSockets() {
        return inputSockets;
    }

    /**
     * @return An array of <code>Socket</code>s that hold the outputs of this step
     */
    public OutputSocket<?>[] getOutputSockets() {
        return outputSockets;
    }

    /**
     * Performs a graph search and either searches forward or backward through the graph of connections and steps.
     *
     * @param socketList          The sockets to search
     * @param connectionSocketMap Maps the socket that is desired to be found for a given connection.
     * @param recursiveStepMap    The recursive function to call itself on the next step in the graph.
     * @return The Set of steps connected to a given list of sockets
     */
    private Set<Step> getConnectedSteps(Socket<?>[] socketList, Function<Connection, Socket> connectionSocketMap, Function<Step, Set<Step>> recursiveStepMap) {
        Set<Step> connectedSteps = new HashSet<>(socketList.length + 1);
        connectedSteps.add(this);
        for (Socket<?> socket : socketList) {
            for (Connection<?> connection : socket.getConnections()) {
                final Socket<?> connectedSocket = connectionSocketMap.apply(connection);
                connectedSocket.getStep().ifPresent(s -> {
                    connectedSteps.addAll(recursiveStepMap.apply(s));
                });
            }
        }
        return connectedSteps;
    }

    /**
     * Recursively retrieves all of the steps that are connected to the input side of this socket.
     * Only looks at {@link InputSocket InputSockets} for subsequent steps that are visited.
     *
     * @return The full set of step that are attached to this steps and subsequent steps input sockets.
     * This set includes itself.
     */
    public Set<Step> getConnectedInputSteps() {
        return getConnectedSteps(inputSockets, connection -> connection.getOutputSocket(), step -> step.getConnectedInputSteps());
    }

    /**
     * Recursively retrieves all of the steps that are connected to the output side of this socket.
     * Only looks at {@link OutputSocket OutputSockets} for subsequent steps that are visited.
     *
     * @return The full set of step that are attached to this steps and subsequent steps input sockets.
     * This set includes itself.
     */
    public Set<Step> getConnectedOutputSteps() {
        return getConnectedSteps(outputSockets, connection -> connection.getInputSocket(), step -> step.getConnectedOutputSteps());
    }

    @Subscribe
    public void onInputSocketChanged(SocketChangedEvent e) {
        final Socket<?> socket = e.getSocket();

        // If this socket that changed is one of the inputs to this step, run the operation with the new value.
        if (socket.getStep().equals(Optional.of(this)) && socket.getDirection().equals(Socket.Direction.INPUT)) {
            this.operation.perform(inputSockets, outputSockets);
        }
    }
}
