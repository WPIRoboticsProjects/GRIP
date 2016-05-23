package edu.wpi.grip.core.operations.composite;


import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.LinkedSocketHint;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import java.util.List;
import java.util.Optional;

public class ValveOperation implements Operation {

    public static OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Valve")
                    .summary("Toggle an output socket on or off using a boolean")
                    .category(OperationDescription.Category.LOGICAL)
                    .build();

    private final InputSocket<Boolean> switcherSocket;
    private final InputSocket inputSocket; // Intentionally using raw types

    private final OutputSocket outputSocket;

    public ValveOperation(InputSocket.Factory inputSocketFactory, OutputSocket.Factory outputSocketFactory) {
        final LinkedSocketHint linkedSocketHint = new LinkedSocketHint(inputSocketFactory, outputSocketFactory);
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
