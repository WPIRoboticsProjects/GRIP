package edu.wpi.grip.core.operations;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.networktables.NTNumber;
import edu.wpi.grip.core.operations.networktables.NTPublishOperation;
import edu.wpi.grip.core.operations.networktables.NTVector2D;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;

import static org.bytedeco.javacpp.opencv_core.*;

public final class Operations {

    private Operations() { /* no op */}

    public static void addOperations(EventBus eventBus) {
        // Add the default built-in operations to the palette
        eventBus.post(new OperationAddedEvent(new ResizeOperation()));
        eventBus.post(new OperationAddedEvent(new BlurOperation()));
        eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new FindContoursOperation()));
        eventBus.post(new OperationAddedEvent(new FilterContoursOperation()));
        eventBus.post(new OperationAddedEvent(new ConvexHullsOperation()));
        eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        eventBus.post(new OperationAddedEvent(new MaskOperation()));
        eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(Number.class, NTNumber.class, NTNumber::new)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(Point.class, NTVector2D.class, NTVector2D::new)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(Size.class, NTVector2D.class, NTVector2D::new)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(ContoursReport.class)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(BlobsReport.class)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(LinesReport.class)));
        eventBus.post(new OperationAddedEvent(new PublishVideoOperation()));
    }
}
