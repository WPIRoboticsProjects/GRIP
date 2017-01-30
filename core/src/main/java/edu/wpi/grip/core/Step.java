package edu.wpi.grip.core;

import edu.wpi.grip.core.metrics.Timer;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.util.ExceptionWitness;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A step is an instance of an operation in a pipeline.  A step contains a list of input and output
 * sockets, and it runs the operation whenever one of the input sockets changes.
 */
@XStreamAlias(value = "grip:Step")
public class Step {
  private static final Logger logger = Logger.getLogger(Step.class.getName());
  private static final String MISSING_SOCKET_MESSAGE_END = " must have a value to run this step.";

  private final ExceptionWitness witness;
  private final Timer timer;

  private final Operation operation;
  private final OperationDescription description;
  private final List<InputSocket> inputSockets;
  private final List<OutputSocket> outputSockets;
  private final Object removedLock = new Object();
  private boolean removed = false;

  /**
   * @param operation               The operation that is performed at this step.
   * @param description             The description of the operation.
   * @param inputSockets            The input sockets from the operation.
   * @param outputSockets           The output sockets provided by the operation.
   * @param exceptionWitnessFactory A factory used to create an {@link ExceptionWitness}.
   * @param timerFactory            A factory used to create a {@link Timer}.
   */
  Step(Operation operation,
       OperationDescription description,
       List<InputSocket> inputSockets,
       List<OutputSocket> outputSockets,
       ExceptionWitness.Factory exceptionWitnessFactory,
       Timer.Factory timerFactory) {
    this.operation = operation;
    this.description = description;
    this.inputSockets = inputSockets;
    this.outputSockets = outputSockets;
    this.witness = exceptionWitnessFactory.create(this);
    this.timer = timerFactory.create(this);
  }

  /**
   * @return The description for the step.
   */
  public OperationDescription getOperationDescription() {
    return this.description;
  }

  /**
   * @return An array of {@link InputSocket InputSockets} that hold the inputs to this step.
   */
  public ImmutableList<InputSocket> getInputSockets() {
    return ImmutableList.copyOf(inputSockets);
  }

  /**
   * @return A list of {@link OutputSocket OutputSockets} that hold the outputs of this step.
   */
  public ImmutableList<OutputSocket> getOutputSockets() {
    return ImmutableList.copyOf(outputSockets);
  }

  /**
   * Resets all {@link OutputSocket OutputSockets} to their initial value. Should only be used by
   * {@link Step#runPerformIfPossible()}.
   */
  private void resetOutputSockets() {
    for (OutputSocket<?> outputSocket : outputSockets) {
      outputSocket.resetValueToInitial();
    }
  }

  /**
   * The {@link Operation#perform} method should only be called if all {@link
   * InputSocket#getValue()} are not empty. If one input is invalid then the perform method will not
   * run and all output sockets will be assigned to their default values. If no input sockets have
   * changed values, the perform method will not run.
   */
  protected final void runPerformIfPossible() {
    runPerform(false);
  }


  /**
   * The {@link Operation#perform} method should only be called if all {@link
   * InputSocket#getValue()} are not empty. If one input is invalid then the perform method will not
   * run and all output sockets will be assigned to their default values.
   *
   * @param force if this step should be forced to run. If {@code true}, the operation's perform
   *              method will be called if every input is valid regardless of 'dirtiness'.
   */
  protected final void runPerform(boolean force) {
    boolean anyDirty = false; // Keeps track of if there are sockets that are dirty

    for (InputSocket<?> inputSocket : inputSockets) {
      // If there is a socket that isn't present then we have a problem.
      if (!inputSocket.getValue().isPresent()) {
        witness.flagWarning(inputSocket.getSocketHint().getIdentifier()
            + MISSING_SOCKET_MESSAGE_END);
        resetOutputSockets();
        /* Only run the perform method if all of the input sockets are present. */
        return;
      }
      // If one value is true then this will stay true
      anyDirty |= inputSocket.dirtied();
    }
    if (!force && !anyDirty) {
      // If there aren't any dirty inputs don't clear the exceptions, just return
      return;
    }

    try {
      // We need to ensure that if perform disabled is switching states that we don't run the
      // perform method while that is happening.
      synchronized (removedLock) {
        if (!removed) {
          timer.time(this.operation::perform);
        }
      }
    } catch (RuntimeException e) {
      // We do not want to catch all exceptions, only runtime exceptions.
      // This is especially important when it comes to InterruptedExceptions
      final String operationFailedMessage =
          String.format("The %s operation did not perform correctly.",
              getOperationDescription().name());
      logger.log(Level.WARNING, operationFailedMessage, e);
      witness.flagException(e, operationFailedMessage);
      resetOutputSockets();
      return;
    }
    witness.clearException();
  }

  /**
   * Sets this step as having been removed.
   */
  public final void setRemoved() {
    // We need to wait for the perform method to complete before returning.
    // if we don't wait then the perform method could end up being run concurrently with the
    // perform methods execution
    synchronized (removedLock) {
      removed = true;
      operation.cleanUp();
    }
  }

  /**
   * Allows checks to see if this step has had its perform method disabled. If this value ever
   * returns false it will never return true again.
   *
   * @return true if runPerformIfPossible can run successfully
   */
  protected boolean removed() {
    return removed;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("operation", description.name())
        .toString();
  }

  @Singleton
  public static class Factory {
    private final ExceptionWitness.Factory exceptionWitnessFactory;
    private final Timer.Factory timerFactory;

    @Inject
    public Factory(ExceptionWitness.Factory exceptionWitnessFactory,
                   Timer.Factory timerFactory) {
      this.exceptionWitnessFactory = exceptionWitnessFactory;
      this.timerFactory = timerFactory;
    }

    /**
     * @param operationData The operation data to use to construct the step.
     * @return The constructed Step.
     */
    public Step create(OperationMetaData operationData) {
      checkNotNull(operationData, "The operationMetaData cannot be null");
      final Operation operation = operationData.getOperationSupplier().get();
      // Create the list of input and output sockets, and mark this step as their owner.
      final List<InputSocket> inputSockets = operation.getInputSockets();
      final List<OutputSocket> outputSockets = operation.getOutputSockets();

      final Step step = new Step(
          operation,
          operationData.getDescription(),
          inputSockets,
          outputSockets,
          exceptionWitnessFactory,
          timerFactory
      );

      for (Socket<?> socket : inputSockets) {
        socket.setStep(Optional.of(step));
      }
      for (Socket<?> socket : outputSockets) {
        socket.setStep(Optional.of(step));
      }

      return step;
    }
  }

}
