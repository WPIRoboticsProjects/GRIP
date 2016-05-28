package edu.wpi.grip.core;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.*;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.util.List;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
    public static final OperationDescription DESCRIPTION = OperationDescription
            .builder().name("OpenCV Add").summary("Compute the per-pixel sum of two images.").build();
    private SocketHint<Mat>
            aHint = SocketHints.Inputs.createMatSocketHint("a", false),
            bHint = SocketHints.Inputs.createMatSocketHint("b", false),
            sumHint = SocketHints.Inputs.createMatSocketHint("sum", true);

    private InputSocket<Mat> a, b;
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
