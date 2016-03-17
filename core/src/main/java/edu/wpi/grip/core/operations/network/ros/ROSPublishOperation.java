package edu.wpi.grip.core.operations.network.ros;


import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.operations.network.PublishOperation;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * An operation that can publish a type to ROS using the java to message converter
 * @param <S> The type of the socket that is taken as an input to be published
 */
public abstract class ROSPublishOperation<S> extends PublishOperation<S, ROSMessagePublisher> {
    private final ROSNetworkPublisherFactory rosNetworkPublisherFactory;
    private final JavaToMessageConverter<S, ?> converter;

    /*
     * Protected so type resolution can happen
     */
    protected ROSPublishOperation(ROSNetworkPublisherFactory rosNetworkPublisherFactory, JavaToMessageConverter<S, ?> converter) {
        super();
        this.rosNetworkPublisherFactory = rosNetworkPublisherFactory;
        this.converter = converter;
    }

    @Override
    protected final String getNetworkProtocolNameAcronym() {
        return "ROS";
    }

    @Override
    protected final String getNetworkProtocolName() {
        return "Robot Operating System";
    }

    @Override
    protected final String getSocketHintStringPrompt() {
        return "Topic";
    }

    @Override
    protected ROSMessagePublisher createPublisher() {
        return rosNetworkPublisherFactory.create(converter);
    }

    @Override
    protected final void performPublish(S socketValue, ROSMessagePublisher publisher, List<InputSocket<?>> restOfInputSockets) {
        publisher.publish((message, mFactory) -> converter.convert(socketValue, message, mFactory));
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/rosorg-logo.png"));
    }
}
