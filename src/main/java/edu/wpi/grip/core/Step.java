package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.SocketChangedEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A step is an instance of an operation in a pipeline.  A step contains a list of input and output sockets, and it
 * runs the operation whenever one of the input sockets changes.
 */
public class Step {
    private Operation operation;
    private Socket<?>[] inputSockets;
    private Socket<?>[] outputSockets;

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
            socket.setStep(this);
        }

        outputSockets = operation.createOutputSockets(eventBus);
        for (Socket<?> socket : outputSockets) {
            socket.setStep(this);
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
    public Socket<?>[] getInputSockets() {
        return inputSockets;
    }

    /**
     * @return An array of <code>Socket</code>s that hold the outputs of this step
     */
    public Socket<?>[] getOutputSockets() {
        return outputSockets;
    }

    @Subscribe
    public void onInputSocketChanged(SocketChangedEvent e) {
        // If this socket that changed is one of the inputs to this step, run the operation with the new value.
        if (e.getSocket().getStep() == this) {
            operation.perform(inputSockets, outputSockets);
        }
    }
}
