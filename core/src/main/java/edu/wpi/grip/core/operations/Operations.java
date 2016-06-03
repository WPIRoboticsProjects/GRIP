package edu.wpi.grip.core.operations;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.OperationMetaData;
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
import edu.wpi.grip.core.operations.templated.TemplateFactory;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Size;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class Operations {

    private final EventBus eventBus;

    private final ImmutableList<OperationMetaData> operations;

    @Inject
    Operations(EventBus eventBus,
               @Named("ntManager") MapNetworkPublisherFactory ntPublisherFactory,
               @Named("rosManager") ROSNetworkPublisherFactory rosPublishFactory,
               InputSocket.Factory isf,
               OutputSocket.Factory osf) {
        this.eventBus = checkNotNull(eventBus, "EventBus cannot be null");
        checkNotNull(ntPublisherFactory, "ntPublisherFactory cannot be null");
        checkNotNull(rosPublishFactory, "rosPublishFactory cannot be null");
        TemplateFactory templateFactory = new TemplateFactory(isf, osf);

        this.operations = ImmutableList.of(
                // Composite operations
                new OperationMetaData(BlurOperation.DESCRIPTION, () -> new BlurOperation(isf, osf)),
                new OperationMetaData(ConvexHullsOperation.DESCRIPTION, () -> new ConvexHullsOperation(isf, osf)),
                new OperationMetaData(DesaturateOperation.DESCRIPTION, () -> new DesaturateOperation(isf, osf)),
                new OperationMetaData(DistanceTransformOperation.DESCRIPTION, () -> new DistanceTransformOperation(isf, osf)),
                new OperationMetaData(FilterContoursOperation.DESCRIPTION, () -> new FilterContoursOperation(isf, osf)),
                new OperationMetaData(FilterLinesOperation.DESCRIPTION, () -> new FilterLinesOperation(isf, osf)),
                new OperationMetaData(FindBlobsOperation.DESCRIPTION, () -> new FindBlobsOperation(isf, osf)),
                new OperationMetaData(FindContoursOperation.DESCRIPTION, () -> new FindContoursOperation(isf, osf)),
                new OperationMetaData(FindLinesOperation.DESCRIPTION, () -> new FindLinesOperation(isf, osf)),
                new OperationMetaData(HSLThresholdOperation.DESCRIPTION, () -> new HSLThresholdOperation(isf, osf)),
                new OperationMetaData(HSVThresholdOperation.DESCRIPTION, () -> new HSVThresholdOperation(isf, osf)),
                new OperationMetaData(MaskOperation.DESCRIPTION, () -> new MaskOperation(isf, osf)),
                new OperationMetaData(NormalizeOperation.DESCRIPTION, () -> new NormalizeOperation(isf, osf)),
                new OperationMetaData(PublishVideoOperation.DESCRIPTION, () -> new PublishVideoOperation(isf)),
                new OperationMetaData(ResizeOperation.DESCRIPTION, () -> new ResizeOperation(isf, osf)),
                new OperationMetaData(RGBThresholdOperation.DESCRIPTION, () -> new RGBThresholdOperation(isf, osf)),
                new OperationMetaData(SwitchOperation.DESCRIPTION, () -> new SwitchOperation(isf, osf)),
                new OperationMetaData(ValveOperation.DESCRIPTION, () -> new ValveOperation(isf, osf)),
                new OperationMetaData(WatershedOperation.DESCRIPTION, () -> new WatershedOperation(isf, osf)),
                new OperationMetaData(ThresholdMoving.DESCRIPTION, () -> new ThresholdMoving(isf, osf)),

                new OperationMetaData(
                        OperationDescription.builder()
                                .name("Crop")
                                .summary("Crop an image")
                                .icon(Icon.iconStream("grip"))
                                .build(),
                        templateFactory.create(
                                SocketHints.Inputs.createMatSocketHint("src", false),
                                SocketHints.Inputs.createPointSocketHint("p1", false),
                                SocketHints.Inputs.createPointSocketHint("p2", false),
                                SocketHints.Inputs.createMatSocketHint("dst", true),
                                (src, p1, p2, dst) -> {
                                    final Rect rect = new Rect(p1, p2);
                                    final Mat tmp = new Mat(src, rect);
                                    tmp.copyTo(dst);
                                }
                        )),

                new OperationMetaData(
                        OperationDescription.builder()
                                .name("Number Threshold")
                                .summary("Returns a boolean on whether or not the number is within the given range.")
                                .icon(Icon.iconStream("grip"))
                                .build(),
                        templateFactory.createReturning(
                                SocketHints.Inputs.createNumberSpinnerSocketHint("Input", 0),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("Min", -1),
                                SocketHints.Inputs.createNumberSpinnerSocketHint("Max", 1),
                                SocketHints.Outputs.createBooleanSocketHint("Output", true),
                                (num, min, max) -> min.doubleValue() <= num.doubleValue() && max.doubleValue() >= num.doubleValue()
                        )
                ),

                new OperationMetaData(
                        OperationDescription.builder()
                                .name("Count Contours")
                                .summary("Counts the number of contours in a contours report.")
                                .icon(Icon.iconStream("grip"))
                                .build(),
                        templateFactory.createReturning(
                                new SocketHint.Builder<>(ContoursReport.class).identifier("Contours").build(),
                                SocketHints.Outputs.createNumberSocketHint("Count", 0),
                                (contours) -> contours.getContours().size()
                        )
                ),

                // OpenCV operations
                new OperationMetaData(MatFieldAccessor.DESCRIPTION, () -> new MatFieldAccessor(isf, osf)),
                new OperationMetaData(MinMaxLoc.DESCRIPTION, () -> new MinMaxLoc(isf, osf)),
                new OperationMetaData(NewPointOperation.DESCRIPTION, () -> new NewPointOperation(isf, osf)),
                new OperationMetaData(NewSizeOperation.DESCRIPTION, () -> new NewSizeOperation(isf, osf)),

                // NetworkTables publishing operations
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(ContoursReport.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, ContoursReport.class, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(LinesReport.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, LinesReport.class, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(BlobsReport.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, BlobsReport.class, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Size.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, Size.class, Vector2D.class, Vector2D::new, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Point.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, Point.class, Vector2D.class, Vector2D::new, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Number.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, Number.class, NumberPublishable.class, NumberPublishable::new, ntPublisherFactory)),
                new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Boolean.class),
                        () -> new NTPublishAnnotatedOperation<>(isf, Boolean.class, BooleanPublishable.class, BooleanPublishable::new, ntPublisherFactory)),

                // ROS publishing operations
                new OperationMetaData(ROSPublishOperation.descriptionFor(Number.class),
                        () -> new ROSPublishOperation<>(isf, Number.class, rosPublishFactory, JavaToMessageConverter.FLOAT)),
                new OperationMetaData(ROSPublishOperation.descriptionFor(Boolean.class),
                        () -> new ROSPublishOperation<>(isf, Boolean.class, rosPublishFactory, JavaToMessageConverter.BOOL)),
                new OperationMetaData(ROSPublishOperation.descriptionFor(ContoursReport.class),
                        () -> new ROSPublishOperation<>(isf, ContoursReport.class, rosPublishFactory, JavaToMessageConverter.CONTOURS)),
                new OperationMetaData(ROSPublishOperation.descriptionFor(BlobsReport.class),
                        () -> new ROSPublishOperation<>(isf, BlobsReport.class, rosPublishFactory, JavaToMessageConverter.BLOBS)),
                new OperationMetaData(ROSPublishOperation.descriptionFor(LinesReport.class),
                        () -> new ROSPublishOperation<>(isf, LinesReport.class, rosPublishFactory, JavaToMessageConverter.LINES))
        );
    }

    @VisibleForTesting
    ImmutableList<OperationMetaData> operations() {
        return operations;
    }

    public void addOperations() {
        operations.stream()
                .map(OperationAddedEvent::new)
                .forEach(eventBus::post);
    }
}
