package edu.wpi.grip.core.operations.network;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import edu.wpi.grip.core.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Publishes data to a specific network protocol.
 * This class does not actually perform the publishing, that is the role of the {@link NetworkKeyValuePublisher}.
 * Its exclusive responsibility is resolving the reflection required to get the value that should be published.
 *
 * @param <S> The socketType of socket that can be connected to this step
 * @param <T> A class implementing {@link Publishable} that determines what data is Published
 * @param <P> The socketType that every func
 */
public abstract class KeyValuePublishOperation<S, T extends Publishable, P> implements Operation {
    private final Manager manager;
    private final TypeToken<S> socketType;
    private final TypeToken<P> publishType;
    private final Function<S, T> converter;
    private final ImmutableList<Invokable<T, P>> ntValueMethods;
    private final ImmutableSet<String> keys;

    /**
     * Create a new publish operation for a socket socketType that implements {@link Publishable} directly
     */
    @SuppressWarnings("unchecked")
    public KeyValuePublishOperation(Manager manager) {
        this(manager, value -> (T) value);
    }

    /**
     * Create a new publish operation where the socket socketType and Publishable socketType are different.  This is useful for
     * classes that we don't create, such as JavaCV's {@link org.bytedeco.javacpp.opencv_core.Size} class, since we
     * can't make them implement additional interfaces.
     *
     * @param converter  A function to convert socket values into publishable values
     */
    public KeyValuePublishOperation(Manager manager, Function<S, T> converter) {
        this.manager = checkNotNull(manager, "Manager was null");
        this.socketType = new TypeToken<S>(getClass()) {
        };
        this.publishType = new TypeToken<P>(getClass()) {
        };
        final TypeToken<T> reportType = new TypeToken<T>(getClass()) {
        };
        this.converter = checkNotNull(converter, "Converter was null");

        final Comparator<Invokable<T, P>> byWeight = Comparator.comparing(method -> method.getAnnotation(PublishValue.class).weight());

        // Any accessor method with an @PublishValue annotation can be published to NetworkTables.  We sort them by their
        // "weights" in order to avoid the issue of different JVM versions returning methods in a different order.
        this.ntValueMethods = ImmutableList.copyOf(Arrays.asList(reportType.getRawType().getDeclaredMethods()).stream()
                .filter(method -> method.getAnnotation(PublishValue.class) != null)
                .map(m -> reportType.method(m).returning(publishType))
                .sorted(byWeight)
                .iterator());
        if (this.ntValueMethods.size() == 0){
            throw new IllegalArgumentException("Must be at least one @PublishValue method on " + reportType + " to be published");
        }

        // In order for KeyValuePublishOperation to call the accessor methods, they must all be public
        this.ntValueMethods.stream()
                .filter(m -> !m.isPublic())
                .findAny()
                .ifPresent(m -> {
                    throw new IllegalArgumentException("@PublishValue method must be public: " + m);
                });

        // In order for KeyValuePublishOperation to call the accessor methods, they must all have no parameters
        this.ntValueMethods.stream()
                .filter(method -> method.getParameters().size() > 0)
                .findAny()
                .ifPresent(method -> {
                    throw new IllegalArgumentException("@PublishValue method must have 0 parameters: " + method);
                });

        final long uniqueKeyCount = this.ntValueMethods
                .stream().map(method -> method.getAnnotation(PublishValue.class).key()).distinct().count();
        if (uniqueKeyCount != this.ntValueMethods.size()) {
            throw new IllegalArgumentException("@PublishValue methods must have distinct keys: " + reportType);
        }
        this.keys = ImmutableSet
                .copyOf(this.ntValueMethods.stream().map(method -> method.getAnnotation(PublishValue.class).key())
                        .filter(k -> !k.isEmpty()) // Check that the key is not empty
                        .iterator());

        // Count the number of method annotated without a key
        final long count = this.ntValueMethods.stream()
                .filter(method -> method.getAnnotation(PublishValue.class).key().isEmpty())
                .count();
        // If there is more than one method without a key
        // or, if there is one method without a key, it must be the only method
        if (count > 1 || (count != 0 && this.ntValueMethods.size() > 1)) {
            throw new IllegalArgumentException("If there is more than one @PublishValue method all need keys: " + reportType);
        }

        // The weight thing doesn't help us if two methods have the same weight, since the JVM could put them in either
        // order.
        if (!Ordering.from(byWeight).isStrictlyOrdered(this.ntValueMethods)) {
            throw new IllegalArgumentException("@PublishValue methods must have distinct weights: " + reportType);
        }
    }

    @Override
    public String getName() {
        return getNetworkProtocolNameAcronym() + "Publish " + socketType.getRawType().getSimpleName();
    }

    @Override
    public String getDescription() {
        return "Publish a " + socketType.getRawType().getSimpleName() + " to " + getNetworkProtocolName();
    }

    @Override
    public Category getCategory() {
        return Category.NETWORK;
    }

    protected final TypeToken<S> getSocketType() {
        return socketType;
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        final InputSocket<?>[] sockets = new InputSocket[2 + ntValueMethods.size()];
        int i = 0;

        // Create an input for the actual object being published
        sockets[i++] = new InputSocket<>(eventBus,
                new SocketHint.Builder<>(socketType.getRawType()).identifier("Value").build());

        // Create a string input for the key used by the network protocol
        sockets[i++] = new InputSocket<>(eventBus,
                SocketHints.Inputs.createTextSocketHint(getSocketHintStringPrompt(), "my" + socketType.getRawType().getSimpleName()));

        // Create a checkbox for every property of the object that might be published.  For example, for a
        // ContourReport, the user might wish to publish the x and y coordinates of the center of each contour.
        for (Invokable method : ntValueMethods) {
            sockets[i++] = new InputSocket<>(eventBus,
                    SocketHints.createBooleanSocketHint("Publish " + method.getAnnotation(PublishValue.class).key(), true));
        }

        return sockets;
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[0];
    }


    /**
      There should be a different instance of {@link NetworkKeyValuePublisher} for each step.
     * The NetworkKeyValuePublisher will be closed by the close function when this step is removed.
     * @return The publisher that will be used for this step.
     */
    @Override
    public Optional<?> createData() {
        return Optional.of(manager.createPublisher(publishType.getRawType(), keys));
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        int i = 0;

        final T value = converter.apply((S) inputs[i++].getValue().get());
        final String subtableName = (String) inputs[i++].getValue().get();

        if (subtableName.isEmpty()) {
            throw new IllegalArgumentException("Need key to publish to " + getNetworkProtocolName());
        }

        final NetworkKeyValuePublisher<P> publisher = (NetworkKeyValuePublisher<P>) data.get();
        // We do this every time to ensure that publisher knows about any new values
        publisher.setName(subtableName);


        // For each PublishValue method in the object being published, put it in the table if the the corresponding
        // checkbox is selected.
        final Map<String, P> publishMap = Maps.newHashMapWithExpectedSize(ntValueMethods.size());
        try {
            for (final Invokable<T, P> method : ntValueMethods) {
                final String key = method.getAnnotation(PublishValue.class).key();
                final boolean publish = (Boolean) inputs[i++].getValue().get();

                if (publish) {
                    publishMap.put(key, method.invoke(value));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwables.propagate(e);
        }

        publisher.publish(publishMap);

    }

    @Override
    public void cleanUp(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        final NetworkKeyValuePublisher networkPublisher = (NetworkKeyValuePublisher) data.get();
        networkPublisher.close();
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
