package edu.wpi.grip.core.operations.templated;

import edu.wpi.grip.core.operations.CudaOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.common.collect.ImmutableList;

import java.util.List;

final class FourSourceOneDestinationCudaOperation<T1, T2, T3, T4, R> extends CudaOperation {

  private final InputSocket<T1> input1;
  private final InputSocket<T2> input2;
  private final InputSocket<T3> input3;
  private final InputSocket<T4> input4;
  private final OutputSocket<R> output;
  private final Performer<T1, T2, T3, T4, R> performer;

  FourSourceOneDestinationCudaOperation(InputSocket.Factory isf,
                                        OutputSocket.Factory osf,
                                        SocketHint<T1> t1SocketHint,
                                        SocketHint<T2> t2SocketHint,
                                        SocketHint<T3> t3SocketHint,
                                        SocketHint<T4> t4SocketHint,
                                        SocketHint<R> rSocketHint,
                                        Performer<T1, T2, T3, T4, R> performer) {
    super(isf, osf);
    this.input1 = isf.create(t1SocketHint);
    this.input2 = isf.create(t2SocketHint);
    this.input3 = isf.create(t3SocketHint);
    this.input4 = isf.create(t4SocketHint);
    this.output = osf.create(rSocketHint);
    this.performer = performer;
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(
        input1,
        input2,
        input3,
        input4,
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
        input3.getValue().get(),
        input4.getValue().get(),
        preferCuda(),
        output.getValue().get()
    );
    output.flagChanged();
  }

  @FunctionalInterface
  public interface Performer<T1, T2, T3, T4, R> {
    void perform(T1 src1, T2 src2, T3 src3, T4 src4, boolean preferCuda, R dst);
  }

}
