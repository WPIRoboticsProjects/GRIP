package edu.wpi.grip.core.operations.network.ros;


import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.operations.network.NetworkPublishOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.util.Icon;

import java.util.Collections;
import java.util.List;

/**
 * An operation that can publish a type to ROS using the java to message converter
 * @param <D> The type of the socket that is taken as an input to be published
 */
public class ROSPublishOperation<D> extends NetworkPublishOperation<D> {

    public static OperationDescription descriptionFor(Class<?> dataType) {
        return OperationDescription.builder()
                .name("ROSPublish " + dataType.getSimpleName())
                .summary("Publishes a " + dataType.getSimpleName() + " to a ROS node")
                .icon(Icon.iconStream("rosorg-logo"))
                .category(OperationDescription.Category.NETWORK)
                .build();
    }

    private final JavaToMessageConverter<D, ?> converter;
    private final ROSMessagePublisher publisher;


    public ROSPublishOperation(InputSocket.Factory inputSocketFactory, Class<D> dataType, ROSNetworkPublisherFactory rosNetworkPublisherFactory, JavaToMessageConverter<D, ?> converter) {
        super(inputSocketFactory, dataType);
        this.converter = converter;
        this.publisher = rosNetworkPublisherFactory.create(converter);
    }

    @Override
    protected List<InputSocket<Boolean>> createFlagSockets() {
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void doPublish() {
        publisher.publish((m, f) -> converter.convert(dataSocket.getValue().get(), m, f));
    }

}
