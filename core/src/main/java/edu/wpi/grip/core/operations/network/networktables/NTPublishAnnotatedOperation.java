package edu.wpi.grip.core.operations.network.networktables;

import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.PublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.PublishValue;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.util.Icon;

import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An operation that publishes any type that implements {@link Publishable} to NetworkTables.
 * <p>
 * To be publishable, a type should have one or more accessor methods annotated with {@link PublishValue}.  This is done
 * with annotations instead of methods
 *
 * @param <D> the type of the data to publish (e.g. {@link Number})
 * @param <P> the publishable analog of the data (e.g. {@link edu.wpi.grip.core.operations.network.NumberPublishable NumberPublishable})
 */
public class NTPublishAnnotatedOperation<D, P extends Publishable> extends PublishAnnotatedOperation<D, P> {

    /**
     * Creates an {@code OperationDescription} for an {@link NTPublishAnnotatedOperation} that publishes data of the given type.
     *
     * @param dataType the type of the data to publish
     */
    public static OperationDescription descriptionFor(Class<?> dataType) {
        checkNotNull(dataType);
        final String name = dataType.getSimpleName();
        return OperationDescription.builder()
                .name(String.format("NTPublish %s", name))
                .summary(String.format("Publishes a %s to a network table", name))
                .aliases(String.format("Publish %s", name))
                .icon(Icon.iconStream("first"))
                .category(OperationDescription.Category.NETWORK)
                .build();
    }


    /**
     * Creates an NTPublishAnnotatedOperation for a type that already implements {@link Publishable} (e.g.
     * {@link edu.wpi.grip.core.operations.composite.ContoursReport ContoursReport}).
     *
     * @param inputSocketFactory factory for creating the input sockets
     * @param dataType           the type of the data to publish
     * @param publisherFactory   factory for creating the publisher
     */
    public NTPublishAnnotatedOperation(InputSocket.Factory inputSocketFactory,
                                       Class<P> dataType,
                                       MapNetworkPublisherFactory publisherFactory) {
        this(inputSocketFactory, (Class<D>) dataType, dataType, d -> (P) d, publisherFactory);
    }

    /**
     * Creates an NTPublishAnnotatedOperation.
     *
     * @param inputSocketFactory factory for creating the input sockets
     * @param dataType           the type of the data to publish
     * @param publishType        the publishable analog of the data type
     * @param converter          function for converting instances of the input type to publishable objects
     * @param publisherFactory   factory for creating the publisher
     */
    public NTPublishAnnotatedOperation(InputSocket.Factory inputSocketFactory,
                                       Class<D> dataType,
                                       Class<P> publishType,
                                       Function<D, P> converter,
                                       MapNetworkPublisherFactory publisherFactory) {
        super(inputSocketFactory, dataType, publishType, converter, publisherFactory);
        super.nameSocket.setValue("my" + dataType.getSimpleName());
    }

}
