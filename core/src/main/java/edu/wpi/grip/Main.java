package edu.wpi.grip;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.generated.CVOperations;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main {
    public static class Core {
        public final EventBus eventBus;
        public final Pipeline pipeline;
        public final Runnable loadOperations;

        private Core(final EventBus eventBus, final Pipeline pipeline, final Runnable loadOperations) {
            this.eventBus = eventBus;
            this.pipeline = pipeline;
            this.loadOperations = loadOperations;
        }
    }

    protected Core createNewCore() {
        class PostBackHandler implements SubscriberExceptionHandler {
            private EventBus eventBus;

            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                eventBus.post(new FatalErrorEvent(exception));
            }
        }
        final PostBackHandler postBackHandler = new PostBackHandler();
        final EventBus eventBus = new EventBus(postBackHandler);
        postBackHandler.eventBus = eventBus;
        return new Core(eventBus, new Pipeline(eventBus), () -> Main.loadAllOperations(eventBus));
    }

    public static void main(final String... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        System.out.println("Running in headless mode");
        final Main main = new Main();
        final Core core = main.createNewCore();
        core.loadOperations.run();
        main.runDemo(core.eventBus);
    }

    private void runDemo(EventBus eventBus) throws IOException {
        final Source networkSource = new CameraSource(eventBus, "http://axis-camera.local/mjpg/video.mjpg");
        final Operation matFieldAccessor = new MatFieldAccessor();
        final Step matFieldAccessorStep = new Step(eventBus, matFieldAccessor);
        eventBus.post(new StepAddedEvent(matFieldAccessorStep));
        eventBus.post(new ConnectionAddedEvent(new Connection(eventBus, networkSource.getOutputSockets()[0], matFieldAccessorStep.getInputSockets()[0])));
        Arrays.asList(matFieldAccessorStep.getOutputSockets())
                .stream()
                .filter(outputSocket -> outputSocket.getSocketHint().isPublishable())
                .forEach(outputSocket1 -> outputSocket1.setPublished(true));
    }

    private static final void loadAllOperations(EventBus eventBus ) {
        // Add the default built-in operations to the palette
        eventBus.post(new OperationAddedEvent(new BlurOperation()));
        eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        eventBus.post(new OperationAddedEvent(new MaskOperation()));
        eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));

        // Add all of the auto-generated OpenCV operations
        CVOperations.addOperations(eventBus);

        eventBus.post(new SetSinkEvent(new DummySink()));
    }
}
