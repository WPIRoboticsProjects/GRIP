package edu.wpi.grip.core.operations;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.generated.CVOperations;

public class OpenCVOperations extends CVOperations {

    /**
     * Adds all of the generated OpenCV operations to the pipeline
     * @param eventBus The Guava Event Bus
     */
    public static void publishAllOperations(final EventBus eventBus){
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));
        OPERATIONS.stream()
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
    }
}
