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
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.List;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
  public static final OperationDescription DESCRIPTION = OperationDescription
      .builder().name("OpenCV Add").summary("Compute the per-pixel sum of two images.").build();
  private final SocketHint<Mat> aHint = SocketHints.Inputs.createMatSocketHint("a", false);
  private final SocketHint<Mat> bHint = SocketHints.Inputs.createMatSocketHint("b", false);
  private final SocketHint<Mat> sumHint = SocketHints.Inputs.createMatSocketHint("sum", true);

  private InputSocket<Mat> a;
  private InputSocket<Mat> b;
  private OutputSocket<Mat> sum;

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
    opencv_core.add(a.getValue().get(), b.getValue().get(), sum.getValue().get());
  }
}
