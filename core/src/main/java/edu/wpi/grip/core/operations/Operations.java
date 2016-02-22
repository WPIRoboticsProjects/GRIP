package edu.wpi.grip.core.operations;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.network.BooleanPublishable;
import edu.wpi.grip.core.operations.network.Manager;
import edu.wpi.grip.core.operations.network.NumberPublishable;
import edu.wpi.grip.core.operations.network.Vector2D;
import edu.wpi.grip.core.operations.network.networktables.NTKeyValuePublishOperation;
import edu.wpi.grip.core.operations.network.ros.ROSKeyValuePublishOperation;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.Size;

@Singleton
public class Operations {
    private final EventBus eventBus;
    private final ImmutableList<Supplier<Operation>> operations;

    @Inject
    Operations(EventBus eventBus, @Named("ntManager") Manager ntManager, @Named("rosManager") Manager rosManager) {
        this.eventBus = checkNotNull(eventBus, "EventBus cannot be null");
        checkNotNull(ntManager, "ntManager cannot be null");
        checkNotNull(rosManager, "rosManager cannot be null");
        this.operations = ImmutableList.of(
                ResizeOperation::new,
                BlurOperation::new,
                DesaturateOperation::new,
                RGBThresholdOperation::new,
                HSVThresholdOperation::new,
                HSLThresholdOperation::new,
                FindContoursOperation::new,
                FilterContoursOperation::new,
                ConvexHullsOperation::new,
                FindBlobsOperation::new,
                FindLinesOperation::new,
                FilterLinesOperation::new,
                MaskOperation::new,
                MinMaxLoc::new,
                MatFieldAccessor::new,
                NewPointOperation::new,
                NewSizeOperation::new,
                () -> new NTKeyValuePublishOperation<Number, NumberPublishable, Number>(ntManager, NumberPublishable::new) {
                },
                () -> new NTKeyValuePublishOperation<Boolean, BooleanPublishable, Boolean>(ntManager, BooleanPublishable::new) {
                },
                () -> new NTKeyValuePublishOperation<Point, Vector2D, Double>(ntManager, Vector2D::new) {
                },
                () -> new NTKeyValuePublishOperation<Size, Vector2D, Double>(ntManager, Vector2D::new) {
                },
                () -> new NTKeyValuePublishOperation<ContoursReport, ContoursReport, double[]>(ntManager) {
                },
                () -> new NTKeyValuePublishOperation<BlobsReport, BlobsReport, double[]>(ntManager) {
                },
                () -> new NTKeyValuePublishOperation<LinesReport, LinesReport, double[]>(ntManager) {
                },
                () -> new ROSKeyValuePublishOperation<Number, NumberPublishable, Number>(ntManager, NumberPublishable::new) {
                },
                () -> new ROSKeyValuePublishOperation<Boolean, BooleanPublishable, Boolean>(ntManager, BooleanPublishable::new) {
                },
                () -> new ROSKeyValuePublishOperation<Point, Vector2D, Double>(ntManager, Vector2D::new) {
                },
                () -> new ROSKeyValuePublishOperation<Size, Vector2D, Double>(ntManager, Vector2D::new) {
                },
                () -> new ROSKeyValuePublishOperation<ContoursReport, ContoursReport, double[]>(ntManager) {
                },
                () -> new ROSKeyValuePublishOperation<BlobsReport, BlobsReport, double[]>(ntManager) {
                },
                () -> new ROSKeyValuePublishOperation<LinesReport, LinesReport, double[]>(ntManager) {
                },
                PublishVideoOperation::new,
                DistanceTransformOperation::new,
                NormalizeOperation::new,
                WatershedOperation::new
        );
    }

    public void addOperations() {
        operations.stream()
                .map(s -> s.get())
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
    }
}
