package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class AdditionOperation implements Operation {
  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Add")
          .summary("Compute the sum of two doubles")
          .build();
  private final SocketHint<Number> aHint = SocketHints.createNumberSocketHint("a", 0.0);
  private final SocketHint<Number> bHint = SocketHints.createNumberSocketHint("b", 0.0);
  private final SocketHint<Number> cHint = SocketHints.Outputs.createNumberSocketHint("c", 0.0);

  private final InputSocket<Number> a;
  private final InputSocket<Number> b;
  private final OutputSocket<Number> c;

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
    double valA = a.getValue().get().doubleValue();
    double valB = b.getValue().get().doubleValue();
    double valC = valA + valB;
    c.setValue(valC);
  }
}
