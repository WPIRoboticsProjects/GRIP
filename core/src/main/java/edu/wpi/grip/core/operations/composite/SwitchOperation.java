package edu.wpi.grip.core.operations.composite;


import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.LinkedSocketHint;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;

/**
 * Allows for switching between two arbitrary typed {@link Socket} using a boolean {@link
 * InputSocket}.
 */
@Description(name = "Switch",
             summary = "Switch between two possible input sockets using a boolean",
             category = OperationCategory.LOGICAL)
public class SwitchOperation implements Operation {

  private final InputSocket<Boolean> switcherSocket;
  private final InputSocket inputSocket1; // Intentionally using raw types
  private final InputSocket inputSocket2;

  private final OutputSocket<?> outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public SwitchOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    final SocketHint<Boolean> switcherHint = SocketHints.createBooleanSocketHint("switch", true);
    final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(inputSocketFactory,
        outputSocketFactory);

    this.switcherSocket = inputSocketFactory.create(switcherHint);
    this.inputSocket1 = linkedSocketHint.linkedInputSocket("If True");
    this.inputSocket2 = linkedSocketHint.linkedInputSocket("If False");

    this.outputSocket = linkedSocketHint.linkedOutputSocket("Result");
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        switcherSocket,
        inputSocket1,
        inputSocket2
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public void perform() {
    // If the input is true pass one value through
    if (switcherSocket.getValue().get()) {
      outputSocket.setValueOptional(inputSocket1.getValue());
    } else { // Otherwise pass the other one through
      outputSocket.setValueOptional(inputSocket2.getValue());
    }
  }
}
