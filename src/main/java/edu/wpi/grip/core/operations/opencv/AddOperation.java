package edu.wpi.grip.core.operations.opencv;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.*;


/**
 * Performs the opencv add operation
 */
public class AddOperation implements Operation {
    private SocketHint<Mat>
            aHint = new SocketHint<>("a", Mat.class),
            bHint = new SocketHint<>("b", Mat.class),
            sumHint = new SocketHint<>("sum", Mat.class);

    @Override
    public String getName() {
        return "Add";
    }

    @Override
    public String getDescription() {
        return "Compute the per-pixel sum of two images";
    }

    @Override
    public Socket<Mat>[] createInputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, aHint, new Mat()), new Socket<>(eventBus, bHint, new Mat())};
    }

    @Override
    public Socket<Mat>[] createOutputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, sumHint, new Mat())};
    }

    @Override
    public void perform(Socket[] inputs, Socket[] outputs) {
        Socket<Mat> a = inputs[0], b = inputs[1], sum = outputs[0];
        opencv_core.add(a.getValue(), b.getValue(), sum.getValue());
    }
}
