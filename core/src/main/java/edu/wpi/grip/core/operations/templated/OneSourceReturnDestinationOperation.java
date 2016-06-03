package edu.wpi.grip.core.operations.templated;


import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import java.util.List;

@SuppressWarnings("PMD.GenericsNaming")
class OneSourceReturnDestinationOperation<T1, R> implements Operation {
    private final InputSocket<T1> input1;
    private final OutputSocket<R> output;
    private final Performer<T1, R> performer;

    @FunctionalInterface
    public interface Performer<T1, R> {
        R perform(T1 src1);
    }

    OneSourceReturnDestinationOperation(InputSocket.Factory inputSocketFactory,
                                        OutputSocket.Factory outputSocketFactory,
                                        SocketHint<T1> t1SocketHint,
                                        SocketHint<R> rSocketHint,
                                        Performer<T1, R> performer) {
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
        output.setValue(performer.perform(input1.getValue().get()));
    }
}
