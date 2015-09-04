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
    final private SocketHint<Mat>
            aHint = new SocketHint<>("a", Mat.class),
            bHint = new SocketHint<>("b", Mat.class),
            sumHint = new SocketHint<>("sum", Mat.class);

    @Override
    public Socket<Mat>[] createInputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, aHint), new Socket<>(eventBus, bHint)};
    }

    @Override
    public Socket<Mat>[] createOutputSockets(EventBus eventBus) {
        return new Socket[]{new Socket<>(eventBus, sumHint, new Mat())};
    }

    @Override
    public void perform(Socket[] inputs, Socket[] outputs) {
        Socket<Mat> term1 = inputs[0], term2 = inputs[1], sum = outputs[0];
        opencv_core.add(term1.getValue(), term2.getValue(), sum.getValue());
    }
}
