package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.SocketChangedEvent;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A step is an instance of an operation in a pipeline.  A step contains a list of input and output sockets, and it
 * runs the operation whenever one of the input sockets changes.
 */
@XStreamAlias(value = "grip:Step")
public class Step {

    private final Logger logger =  Logger.getLogger(Step.class.getName());

    private final Operation operation;
    private final InputSocket<?>[] inputSockets;
    private final OutputSocket<?>[] outputSockets;
    private final Optional<?> data;

    @Singleton
    public static class Factory {
        private final EventBus eventBus;

        @Inject
        public Factory(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        public Step create(Operation operation) {
            checkNotNull(operation, "The operation can not be null");
            // Create the list of input and output sockets, and mark this step as their owner.
            final InputSocket<?>[] inputSockets = operation.createInputSockets(eventBus);

            for (Socket<?> socket : inputSockets) {
                eventBus.register(socket);
            }

            final OutputSocket<?>[] outputSockets = operation.createOutputSockets(eventBus);
            for (Socket<?> socket : outputSockets) {
                eventBus.register(socket);
            }

            final Step step = new Step(operation, inputSockets, outputSockets, operation.createData());
            eventBus.register(step);
            for (Socket<?> socket : inputSockets) {
                socket.setStep(Optional.of(step));
            }
            for (Socket<?> socket : outputSockets) {
                socket.setStep(Optional.of(step));
            }

            step.runPerformIfPossible();
            return step;
        }
    }

    /**
     * @param operation     The operation that is performed at this step.
     * @param inputSockets  The input sockets from the operation.
     * @param outputSockets The output sockets provided by the operation.
     * @param data          The data provided by the operation.
     */
    Step(Operation operation, InputSocket<?>[] inputSockets, OutputSocket<?>[] outputSockets, Optional<?> data) {
        this.operation = operation;
        this.inputSockets = inputSockets;
        this.outputSockets = outputSockets;
        this.data = data;
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
     * Resets all {@link OutputSocket OutputSockets} to their initial value.
     * Should only be used by {@link Step#runPerformIfPossible()}
     */
    private void resetOutputSockets() {
        for (OutputSocket<?> outputSocket : outputSockets) {
            outputSocket.resetValueToInitial();
        }
    }

    /**
     * The {@link Operation#perform} method should only be called if all {@link InputSocket#getValue()} are not empty.
     * If one input is invalid then the perform method will not run and all output sockets will be assigned to their
     * default values.
     */
    private synchronized void runPerformIfPossible() {
        try {
            for (InputSocket<?> inputSocket : inputSockets) {
                inputSocket.getValue()
                        .orElseThrow(() -> new NoSuchElementException(
                                inputSocket.getSocketHint().getIdentifier() + " must have a value to run this step."
                        ));
            }
        } catch (NoSuchElementException e) {
            //TODO: show warning icon
            resetOutputSockets();
            return; /* Only run the perform method if all of the input sockets are present. */
        }

        try {
            this.operation.perform(inputSockets, outputSockets, data);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            resetOutputSockets();
        }
    }

    @Subscribe
    public void onInputSocketChanged(SocketChangedEvent e) {
        final Socket<?> socket = e.getSocket();

        // If this socket that changed is one of the inputs to this step, run the operation with the new value.
        if (socket.getStep().equals(Optional.of(this)) && socket.getDirection().equals(Socket.Direction.INPUT)) {
            runPerformIfPossible();
        }
    }
}
