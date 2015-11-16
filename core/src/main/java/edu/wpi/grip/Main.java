package edu.wpi.grip;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.FatalErrorEvent;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.generated.CVOperations;

import java.lang.reflect.InvocationTargetException;

public class Main {
    private static final EventBus eventBus;
    private static final Core core;

    static {
        eventBus = new EventBus((exception, context) -> {
            Main.eventBus.post(new FatalErrorEvent(exception));
        });
        core = new Core(eventBus, new Pipeline(eventBus), () -> loadAllOperations());
    }

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

    protected static Core getCore() {
        return core;
    }

    public static void main(final String... args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("Running in headless mode");
        getCore();
    }

    private static final void loadAllOperations() {
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
