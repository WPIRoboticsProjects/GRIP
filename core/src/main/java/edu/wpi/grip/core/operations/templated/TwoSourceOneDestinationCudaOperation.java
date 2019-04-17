package edu.wpi.grip.core.operations.templated;

import edu.wpi.grip.core.operations.CudaOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.common.collect.ImmutableList;

import java.util.List;

final class TwoSourceOneDestinationCudaOperation<T1, T2, R> extends CudaOperation {

  private final InputSocket<T1> input1;
  private final InputSocket<T2> input2;
  private final OutputSocket<R> output;
  private final Performer<T1, T2, R> performer;

  TwoSourceOneDestinationCudaOperation(InputSocket.Factory isf,
                                       OutputSocket.Factory osf,
                                       SocketHint<T1> t1SocketHint,
                                       SocketHint<T2> t2SocketHint,
                                       SocketHint<R> rSocketHint,
                                       Performer<T1, T2, R> performer) {
    super(isf, osf);
    this.input1 = isf.create(t1SocketHint);
    this.input2 = isf.create(t2SocketHint);
    this.output = osf.create(rSocketHint);
    this.performer = performer;
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        input1,
        input2,
        gpuSocket
    );
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(
        output
    );
  }

  @Override
  public void perform() {
    performer.perform(
        input1.getValue().get(),
        input2.getValue().get(),
        preferCuda(),
        output.getValue().get()
    );
    output.flagChanged();
  }

  @FunctionalInterface
  public interface Performer<T1, T2, R> {
    void perform(T1 src1, T2 src2, boolean preferCuda, R dst);
  }

}
