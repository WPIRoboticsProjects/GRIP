package edu.wpi.grip.core.operations;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.networktables.NTManager;
import edu.wpi.grip.core.operations.networktables.NTPublishOperation;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;

@Singleton
public class Operations {

    private final NTManager ntManager;
    @Inject
    Operations(NTManager ntManager) {
        this.ntManager = ntManager;
    }

    public void addOperations(EventBus eventBus) {
        // Add the default built-in operations to the palette
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
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(ContoursReport.class, ntManager::getBaseTable)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(BlobsReport.class, ntManager::getBaseTable)));
        eventBus.post(new OperationAddedEvent(new NTPublishOperation<>(LinesReport.class, ntManager::getBaseTable)));
    }
}
