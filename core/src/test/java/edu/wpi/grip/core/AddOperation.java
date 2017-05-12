package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocketFactory;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import org.bytedeco.javacpp.opencv_core;

import java.util.List;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
  public static final OperationDescription DESCRIPTION = OperationDescription
      .builder().name("OpenCV Add").summary("Compute the per-pixel sum of two images.").build();
  private final SocketHint<MatWrapper> aHint = SocketHints.createImageSocketHint("a");
  private final SocketHint<MatWrapper> bHint = SocketHints.createImageSocketHint("b");
  private final SocketHint<MatWrapper> sumHint = SocketHints.createImageSocketHint("sum");

  private InputSocket<MatWrapper> a;
  private InputSocket<MatWrapper> b;
  private OutputSocket<MatWrapper> sum;

  public AddOperation(EventBus eventBus) {
    this(new MockInputSocketFactory(eventBus), new MockOutputSocketFactory(eventBus));
  }

  public AddOperation(InputSocket.Factory isf, OutputSocket.Factory osf) {
    a = isf.create(aHint);
    b = isf.create(bHint);
    sum = osf.create(sumHint);
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        a, b
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        sum
    );
  }

  @Override
  public void perform() {
    opencv_core.add(a.getValue().get().getCpu(),
        b.getValue().get().getCpu(),
        sum.getValue().get().rawCpu());
  }
}
