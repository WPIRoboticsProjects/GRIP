package edu.wpi.grip.core;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import java.util.List;

public class AdditionOperation implements Operation {
    public static final OperationDescription DESCRIPTION =
            OperationDescription.builder()
                    .name("Add")
                    .summary("Compute the sum of two doubles")
                    .build();
    private SocketHint<Number>
            aHint = SocketHints.createNumberSocketHint("a", 0.0),
            bHint = SocketHints.createNumberSocketHint("b", 0.0),
            cHint = SocketHints.Outputs.createNumberSocketHint("c", 0.0);

    private InputSocket<Number> a, b;
    private OutputSocket<Number> c;

    public AdditionOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
        a = isf.create(aHint);
        b = isf.create(bHint);
        c = osf.create(cHint);
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(
                a,
                b
        );
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(
                c
        );
    }

    @Override
    public void perform() {
        double val_a = a.getValue().get().doubleValue();
        double val_b = b.getValue().get().doubleValue();
        double val_c = val_a + val_b;
        c.setValue(val_c);
    }
}
