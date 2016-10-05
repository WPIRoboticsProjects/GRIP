package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.core.operations.network.BooleanPublishable;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.NumberPublishable;
import edu.wpi.grip.core.operations.network.Vector2D;
import edu.wpi.grip.core.operations.network.http.HttpPublishOperation;
import edu.wpi.grip.core.operations.network.networktables.NTPublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.ros.ROSPublishOperation;
import edu.wpi.grip.core.sockets.InputSocket;

import com.google.common.collect.ImmutableList;
import com.google.inject.name.Named;

import org.bytedeco.javacpp.opencv_core;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class NetworkOperations implements Operations {

  private final ImmutableList<OperationMetaData> operations;


  @Inject
  NetworkOperations(@Named("ntManager") MapNetworkPublisherFactory ntPublisherFactory,
                    @Named("httpManager") MapNetworkPublisherFactory httpPublishFactory,
                    @Named("rosManager") ROSNetworkPublisherFactory rosPublishFactory,
                    InputSocket.Factory isf) {
    checkNotNull(ntPublisherFactory, "ntPublisherFactory cannot be null");
    checkNotNull(httpPublishFactory, "httpPublisherFactory cannot be null");
    checkNotNull(rosPublishFactory, "rosPublishFactory cannot be null");
    this.operations = ImmutableList
        .of(
            // NetworkTables publishing operations
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(ContoursReport.class),
                () -> new NTPublishAnnotatedOperation<>(isf, ContoursReport.class,
                    ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(LinesReport.class),
                () -> new NTPublishAnnotatedOperation<>(isf, LinesReport.class,
                    ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(BlobsReport.class),
                () -> new NTPublishAnnotatedOperation<>(isf, BlobsReport.class,
                    ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(opencv_core.Size
                .class),
                () -> new NTPublishAnnotatedOperation<>(isf, opencv_core.Size.class, Vector2D.class,
                    Vector2D::new, ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(opencv_core.Point
                .class),
                () -> new NTPublishAnnotatedOperation<>(isf, opencv_core.Point.class, Vector2D
                    .class,
                    Vector2D::new, ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Number.class),
                () -> new NTPublishAnnotatedOperation<>(isf, Number.class, NumberPublishable.class,
                    NumberPublishable::new, ntPublisherFactory)),
            new OperationMetaData(NTPublishAnnotatedOperation.descriptionFor(Boolean.class),
                () -> new NTPublishAnnotatedOperation<>(isf, Boolean.class, BooleanPublishable
                    .class,
                    BooleanPublishable::new, ntPublisherFactory)),

            // ROS publishing operations
            new OperationMetaData(ROSPublishOperation.descriptionFor(Number.class),
                () -> new ROSPublishOperation<>(isf, Number.class, rosPublishFactory,
                    JavaToMessageConverter.FLOAT)),
            new OperationMetaData(ROSPublishOperation.descriptionFor(Boolean.class),
                () -> new ROSPublishOperation<>(isf, Boolean.class, rosPublishFactory,
                    JavaToMessageConverter.BOOL)),
            new OperationMetaData(ROSPublishOperation.descriptionFor(ContoursReport.class),
                () -> new ROSPublishOperation<>(isf, ContoursReport.class, rosPublishFactory,
                    JavaToMessageConverter.CONTOURS)),
            new OperationMetaData(ROSPublishOperation.descriptionFor(BlobsReport.class),
                () -> new ROSPublishOperation<>(isf, BlobsReport.class, rosPublishFactory,
                    JavaToMessageConverter.BLOBS)),
            new OperationMetaData(ROSPublishOperation.descriptionFor(LinesReport.class),
                () -> new ROSPublishOperation<>(isf, LinesReport.class, rosPublishFactory,
                    JavaToMessageConverter.LINES)),

            // HTTP publishing operations
            new OperationMetaData(HttpPublishOperation.descriptionFor(ContoursReport.class),
                () -> new HttpPublishOperation<>(isf, ContoursReport.class, httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(LinesReport.class),
                () -> new HttpPublishOperation<>(isf, LinesReport.class, httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(BlobsReport.class),
                () -> new HttpPublishOperation<>(isf, BlobsReport.class, httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(opencv_core.Size.class),
                () -> new HttpPublishOperation<>(isf, opencv_core.Size.class, Vector2D.class,
                    Vector2D::new,
                    httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(opencv_core.Point.class),
                () -> new HttpPublishOperation<>(isf, opencv_core.Point.class, Vector2D.class,
                    Vector2D::new,
                    httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(Number.class),
                () -> new HttpPublishOperation<>(isf, Number.class, NumberPublishable.class,
                    NumberPublishable::new, httpPublishFactory)),
            new OperationMetaData(HttpPublishOperation.descriptionFor(Boolean.class),
                () -> new HttpPublishOperation<>(isf, Boolean.class, BooleanPublishable.class,
                    BooleanPublishable::new, httpPublishFactory))
        );
  }

  @Override
  public ImmutableList<OperationMetaData> operations() {
    return operations;
  }

}
