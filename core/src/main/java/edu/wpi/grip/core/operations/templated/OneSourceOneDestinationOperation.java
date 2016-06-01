package edu.wpi.grip.core.operations.templated;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import java.util.List;

@SuppressWarnings("PMD.GenericsNaming")
final class OneSourceOneDestinationOperation<T1, R> implements Operation {
    private final InputSocket<T1> input1;
    private final OutputSocket<R> output;
    private final Performer<T1, R> performer;

    @FunctionalInterface
    public interface Performer<T1, R> {
        void perform(T1 src1, R dst);
    }

    OneSourceOneDestinationOperation(InputSocket.Factory inputSocketFactory,
                                     OutputSocket.Factory outputSocketFactory,
                                     Performer<T1, R> performer,
                                     SocketHint<T1> t1SocketHint,
                                     SocketHint<R> rSocketHint) {
        this.input1 = inputSocketFactory.create(t1SocketHint);
        this.output = outputSocketFactory.create(rSocketHint);
        assert output.getValue().isPresent() : TemplateFactory.ASSERTION_MESSAGE;
        this.performer = performer;
    }

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of(input1);
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(output);
    }

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void perform() {
        performer.perform(input1.getValue().get(), output.getValue().get());
        output.setValue(output.getValue().get());
    }

}
