package edu.wpi.grip.generated;

import edu.wpi.grip.generated.opencv_core.*;
import edu.wpi.grip.generated.opencv_imgproc.*;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.OperationAddedEvent;

public class CVOperations {

    public static void addOperations(EventBus eventBus) {
        eventBus.post(new OperationAddedEvent(new Absdiff()));
        eventBus.post(new OperationAddedEvent(new AdaptiveThreshold()));
        eventBus.post(new OperationAddedEvent(new Add()));
        eventBus.post(new OperationAddedEvent(new AddWeighted()));
        eventBus.post(new OperationAddedEvent(new ApplyColorMap()));
        eventBus.post(new OperationAddedEvent(new BitwiseAnd()));
        eventBus.post(new OperationAddedEvent(new BitwiseNot()));
        eventBus.post(new OperationAddedEvent(new BitwiseOr()));
        eventBus.post(new OperationAddedEvent(new BitwiseXor()));
        eventBus.post(new OperationAddedEvent(new Canny()));
        eventBus.post(new OperationAddedEvent(new Compare()));
        eventBus.post(new OperationAddedEvent(new CvtColor()));
        eventBus.post(new OperationAddedEvent(new Dilate()));
        eventBus.post(new OperationAddedEvent(new Divide()));
        eventBus.post(new OperationAddedEvent(new Erode()));
        eventBus.post(new OperationAddedEvent(new ExtractChannel()));
        eventBus.post(new OperationAddedEvent(new Flip()));
        eventBus.post(new OperationAddedEvent(new GaussianBlur()));
        eventBus.post(new OperationAddedEvent(new Laplacian()));
        eventBus.post(new OperationAddedEvent(new Max()));
        eventBus.post(new OperationAddedEvent(new MedianBlur()));
        eventBus.post(new OperationAddedEvent(new Min()));
        eventBus.post(new OperationAddedEvent(new Multiply()));
        eventBus.post(new OperationAddedEvent(new Rectangle()));
        eventBus.post(new OperationAddedEvent(new Resize()));
        eventBus.post(new OperationAddedEvent(new ScaleAdd()));
        eventBus.post(new OperationAddedEvent(new Sobel()));
        eventBus.post(new OperationAddedEvent(new Subtract()));
        eventBus.post(new OperationAddedEvent(new Threshold()));
    }
}
