package edu.wpi.grip.core.operations.templated;


import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.common.collect.ImmutableList;

import java.util.List;


@SuppressWarnings("PMD.GenericsNaming")
final class TwoSourceOneDestinationOperation<T1, T2, R> implements Operation {
  private final InputSocket<T1> input1;
  private final InputSocket<T2> input2;
  private final OutputSocket<R> output;
  private final Performer<T1, T2, R> performer;

  TwoSourceOneDestinationOperation(
      InputSocket.Factory inputSocketFactory,
      OutputSocket.Factory outputSocketFactory,
      SocketHint<T1> t1SocketHint, SocketHint<T2> t2SocketHint, SocketHint<R> rSocketHint,
      Performer<T1, T2, R> performer) {
    this.performer = performer;
    this.input1 = inputSocketFactory.create(t1SocketHint);
    this.input2 = inputSocketFactory.create(t2SocketHint);
    this.output = outputSocketFactory.create(rSocketHint);
    assert output.getValue().isPresent() : TemplateFactory.ASSERTION_MESSAGE;
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.of(input1, input2);
  }

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of(output);
  }

  @Override
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public void perform() {
    performer.perform(input1.getValue().get(), input2.getValue().get(), output.getValue().get());
    output.flagChanged();
  }

  @FunctionalInterface
  public interface Performer<T1, T2, R> {
    void perform(T1 src1, T2 src2, R dst);
  }
}
