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
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.NumberPublishable;
import edu.wpi.grip.core.operations.network.Vector2D;
import edu.wpi.grip.core.operations.network.networktables.NTPublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.ros.ROSPublishOperation;
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
    Operations(EventBus eventBus, @Named("ntManager") MapNetworkPublisherFactory ntPublisherFactory, @Named("rosManager") ROSNetworkPublisherFactory rosPublishFactory) {
        this.eventBus = checkNotNull(eventBus, "EventBus cannot be null");
        checkNotNull(ntPublisherFactory, "ntPublisherFactory cannot be null");
        checkNotNull(rosPublishFactory, "rosPublishFactory cannot be null");
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
                () -> new NTPublishAnnotatedOperation<Number, NumberPublishable, Double>(ntPublisherFactory, NumberPublishable::new) {
                },
                () -> new NTPublishAnnotatedOperation<Boolean, BooleanPublishable, Boolean>(ntPublisherFactory, BooleanPublishable::new) {
                },
                () -> new NTPublishAnnotatedOperation<Point, Vector2D, Double>(ntPublisherFactory, Vector2D::new) {
                },
                () -> new NTPublishAnnotatedOperation<Size, Vector2D, Double>(ntPublisherFactory, Vector2D::new) {
                },
                () -> new NTPublishAnnotatedOperation<ContoursReport, ContoursReport, double[]>(ntPublisherFactory) {
                },
                () -> new NTPublishAnnotatedOperation<BlobsReport, BlobsReport, double[]>(ntPublisherFactory) {
                },
                () -> new NTPublishAnnotatedOperation<LinesReport, LinesReport, double[]>(ntPublisherFactory) {
                },
                () -> new ROSPublishOperation<Number>(rosPublishFactory, JavaToMessageConverter.FLOAT) {
                },
                () -> new ROSPublishOperation<Boolean>(rosPublishFactory, JavaToMessageConverter.BOOL) {
                },
//                () -> new ROSPublishOperation<Point, Vector2D, Double>(rosManager, Vector2D::new) {
//                },
//                () -> new ROSPublishOperation<Size, Vector2D, Double>(rosManager, Vector2D::new) {
//                },
                () -> new ROSPublishOperation<ContoursReport>(rosPublishFactory, JavaToMessageConverter.CONTOURS) {
                },
                () -> new ROSPublishOperation<BlobsReport>(rosPublishFactory, JavaToMessageConverter.BLOBS) {
                },
                () -> new ROSPublishOperation<LinesReport>(rosPublishFactory, JavaToMessageConverter.LINES) {
                },
                PublishVideoOperation::new,
                DistanceTransformOperation::new,
                NormalizeOperation::new,
                WatershedOperation::new,
                SwitchOperation::new,
                ValveOperation::new,
                ThresholdMoving::new
        );
    }

    public void addOperations() {
        operations.stream()
                .map(s -> s.get())
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
    }
}
