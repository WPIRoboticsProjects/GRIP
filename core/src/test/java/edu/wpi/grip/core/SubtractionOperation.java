package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import java.util.List;

public class SubtractionOperation implements Operation {
  public static final OperationDescription DESCRIPTION =
      OperationDescription.builder()
          .name("Subtract")
          .summary("Computer the difference between two doubles")
          .build();
  private final SocketHint<Number> aHint = SocketHints.createNumberSocketHint("a", 0.0);
  private final SocketHint<Number> bHint = SocketHints.createNumberSocketHint("b", 0.0);
  private final SocketHint<Number> cHint = SocketHints.Outputs.createNumberSocketHint("c", 0.0);

  private InputSocket<Number> a;
  private InputSocket<Number> b;
  private OutputSocket<Number> c;

  public SubtractionOperation(EventBus eventBus) {
    this(new MockInputSocketFactory(eventBus), new MockOutputSocketFactory(eventBus));
  }

  public SubtractionOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
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
    c.setValue(a.getValue().get().doubleValue() - b.getValue().get().doubleValue());
  }
}
