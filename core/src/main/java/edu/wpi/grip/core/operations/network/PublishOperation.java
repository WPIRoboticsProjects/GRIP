package edu.wpi.grip.core.operations.network;


import com.google.common.eventbus.EventBus;
import com.google.common.reflect.TypeToken;
import edu.wpi.grip.core.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 * @param <S> The type of the socket that will be published
 * @param <P> The type of the publisher that will be used to publish values
 */
public abstract class PublishOperation<S, P extends NetworkPublisher> implements Operation {
    private final TypeToken<S> socketType;

    protected PublishOperation() {
        this.socketType = new TypeToken<S>(getClass()) {
        };
    }

    @Override
    public final String getName() {
        return getNetworkProtocolNameAcronym() + "Publish " + socketType.getRawType().getSimpleName();
    }

    @Override
    public final String getDescription() {
        return "Publish a " + socketType.getRawType().getSimpleName() + " to " + getNetworkProtocolName();
    }


    @Override
    public final InputSocket<?>[] createInputSockets(EventBus eventBus) {
        final List<InputSocket<?>> customSockets = provideRemainingInputSockets(eventBus);
        final InputSocket<?>[] sockets = new InputSocket[2 + customSockets.size()];
        int i = 0;
        // Create an input for the actual object being published
        sockets[i++] = new InputSocket<>(eventBus,
                new SocketHint.Builder<>(socketType.getRawType()).identifier("Value").build());

        // Create a string input for the key used by the network protocol
        sockets[i++] = new InputSocket<>(eventBus,
                SocketHints.Inputs.createTextSocketHint(getSocketHintStringPrompt(), "my" + socketType.getRawType().getSimpleName()));
        for (InputSocket<?> socket : customSockets) {
            sockets[i++] = socket;
        }
        return sockets;
    }

    @Override
    public final OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[0];
    }

    /**
     * There should be a different instance of {@link NetworkPublisher} for each step.
     * The NetworkPublisher will be closed by the close function when this step is removed.
     *
     * @return The publisher that will be used for this step.
     */
    @Override
    public final Optional<?> createData() {
        return Optional.of(createPublisher());
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        // Get the socket value that should be published
        final S socketValue = (S) socketType.getRawType().cast(inputs[0].getValue().get());
        // Get the subfield
        final String subField = (String) inputs[1].getValue().get();

        if (subField.isEmpty()) {
            throw new IllegalArgumentException("Need key to publish to " + getNetworkProtocolName());
        }

        // The publisher that the data will be published with
        final P publisher = (P) data.get();
        final List<InputSocket<?>> remainingSockets = inputs.length > 2 ? Arrays.asList(inputs).subList(2, inputs.length) : Collections.emptyList();
        publisher.setName(subField);
        performPublish(socketValue, publisher, remainingSockets);
    }

    /**
     * Performs the publish action. Provides the implementer with the resolved type of the socket being published.
     * @param socketValue The resolved socket value that has been provided as an input.
     * @param publisher The publisher to be used to publish the socket value
     * @param restOfInputSockets The remainder of the input sockets that were provided by {@link #provideRemainingInputSockets}
     */
    protected abstract void performPublish(S socketValue, P publisher, List<InputSocket<?>> restOfInputSockets);

    @Override
    public final void cleanUp(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final NetworkPublisher networkPublisher = (NetworkPublisher) data.get();
        networkPublisher.close();
    }

    @Override
    public final Category getCategory() {
        return Category.NETWORK;
    }

    /**
     * Creates a new publisher to be used for publishing data to the network service
     *
     * @return The publisher to be used.
     */
    protected abstract P createPublisher();

    /**
     * Provide any additional input sockets to be used in addition to the default ones
     */
    protected List<InputSocket<?>> provideRemainingInputSockets(EventBus eventBus) {
        return Collections.emptyList();
    }

    /**
     * @return The network protocol's acronym (eg. ROS for Robot Operating System)
     */
    protected abstract String getNetworkProtocolNameAcronym();

    /**
     * @return The network protocol's name (eg. Robot Operating System)
     */
    protected abstract String getNetworkProtocolName();

    /**
     * @return The hint to indicate what you will be publishing to (eg. ROS to a Topic, Network Tables to a Table)
     */
    protected abstract String getSocketHintStringPrompt();
}
