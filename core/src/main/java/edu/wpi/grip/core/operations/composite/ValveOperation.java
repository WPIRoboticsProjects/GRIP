package edu.wpi.grip.core.operations.composite;


import edu.wpi.grip.annotation.operation.Description;
import edu.wpi.grip.annotation.operation.OperationCategory;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.LinkedSocketHint;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.util.List;
import java.util.Optional;

@Description(name = "Valve",
             summary = "Toggle an output socket on or off using a boolean",
             category = OperationCategory.LOGICAL)
public class ValveOperation implements Operation {

  private final InputSocket<Boolean> switcherSocket;
  private final InputSocket inputSocket; // Intentionally using raw types

  private final OutputSocket outputSocket;

  @Inject
  @SuppressWarnings("JavadocMethod")
  public ValveOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory
      outputSocketFactory) {
    final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(inputSocketFactory,
        outputSocketFactory);
    final SocketHint<Boolean> switcherHint = SocketHints.createBooleanSocketHint("valve", true);

    this.switcherSocket = inputSocketFactory.create(switcherHint);
    this.inputSocket = linkedSocketHint.linkedInputSocket("Input");

    this.outputSocket = linkedSocketHint.linkedOutputSocket("Output");
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        switcherSocket,
        inputSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        outputSocket
    );
  }

  @Override
  public void perform() {
    // If the input is true pass the value through
    if (switcherSocket.getValue().get()) {
      outputSocket.setValueOptional(inputSocket.getValue());
    } else {
      outputSocket.setValueOptional(Optional.empty());
    }
  }
}
