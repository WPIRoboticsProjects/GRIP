package edu.wpi.grip.core.operations.network;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.SocketHints;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Publishes data to a specific network protocol.
 * This class does not actually perform the publishing, that is the role of the {@link NetworkPublisher}.
 * Its exclusive responsibility is resolving the reflection required to get the value that should be published.
 *
 * @param <S> The socketType of socket that can be connected to this step
 * @param <T> A class implementing {@link Publishable} that determines what data is Published
 * @param <P> The type that each function in the {@link Publishable} should return
 */
public abstract class PublishAnnotatedOperation<S, T extends Publishable, P> extends PublishOperation<S, MapNetworkPublisher<P>> {
    private final MapNetworkPublisherFactory factory;
    private final TypeToken<S> socketType;
    private final TypeToken<P> publishType;
    private final Function<S, T> converter;
    private final ImmutableList<Invokable<T, P>> valueMethods;
    private final ImmutableSet<String> keys;

    /**
     * Create a new publish operation for a socket socketType that implements {@link Publishable} directly
     */
    @SuppressWarnings("unchecked")
    public PublishAnnotatedOperation(MapNetworkPublisherFactory factory) {
        this(factory, value -> (T) value);
    }

    /**
     * Create a new publish operation where the socket socketType and Publishable socketType are different.  This is useful for
     * classes that we don't create, such as JavaCV's {@link org.bytedeco.javacpp.opencv_core.Size} class, since we
     * can't make them implement additional interfaces.
     *
     * @param converter A function to convert socket values into publishable values
     */
    public PublishAnnotatedOperation(MapNetworkPublisherFactory factory, Function<S, T> converter) {
        this.factory = checkNotNull(factory, "MapNetworkPublisherFactory was null");
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
        this.valueMethods = ImmutableList.copyOf(Arrays.asList(reportType.getRawType().getDeclaredMethods()).stream()
                .filter(method -> method.getAnnotation(PublishValue.class) != null)
                .map(m -> reportType.method(m).returning(publishType.unwrap()))
                .sorted(byWeight)
                .iterator());
        if (this.valueMethods.size() == 0) {
            throw new IllegalArgumentException("Must be at least one @PublishValue method on " + reportType + " to be published");
        }

        // In order for PublishAnnotatedOperation to call the accessor methods, they must all be public
        this.valueMethods.stream()
                .filter(m -> !m.isPublic())
                .findAny()
                .ifPresent(m -> {
                    throw new IllegalArgumentException("@PublishValue method must be public: " + m);
                });

        // In order for PublishAnnotatedOperation to call the accessor methods, they must all have no parameters
        this.valueMethods.stream()
                .filter(method -> method.getParameters().size() > 0)
                .findAny()
                .ifPresent(method -> {
                    throw new IllegalArgumentException("@PublishValue method must have 0 parameters: " + method);
                });

        final long uniqueKeyCount = this.valueMethods
                .stream().map(method -> method.getAnnotation(PublishValue.class).key()).distinct().count();
        if (uniqueKeyCount != this.valueMethods.size()) {
            throw new IllegalArgumentException("@PublishValue methods must have distinct keys: " + reportType);
        }
        this.keys = ImmutableSet
                .copyOf(this.valueMethods.stream().map(method -> method.getAnnotation(PublishValue.class).key())
                        .filter(k -> !k.isEmpty()) // Check that the key is not empty
                        .iterator());

        // Count the number of method annotated without a key
        final long count = this.valueMethods.stream()
                .filter(method -> method.getAnnotation(PublishValue.class).key().isEmpty())
                .count();
        // If there is more than one method without a key
        // or, if there is one method without a key, it must be the only method
        if (count > 1 || (count != 0 && this.valueMethods.size() > 1)) {
            throw new IllegalArgumentException("If there is more than one @PublishValue method all need keys: " + reportType);
        }

        // The weight thing doesn't help us if two methods have the same weight, since the JVM could put them in either
        // order.
        if (!Ordering.from(byWeight).isStrictlyOrdered(this.valueMethods)) {
            throw new IllegalArgumentException("@PublishValue methods must have distinct weights: " + reportType);
        }
    }

    protected final TypeToken<S> getSocketType() {
        return socketType;
    }

    @Override
    public List<InputSocket<?>> provideRemainingInputSockets(EventBus eventBus) {
        // Create a checkbox for every property of the object that might be published.  For example, for a
        // ContourReport, the user might wish to publish the x and y coordinates of the center of each contour.
        return valueMethods
                .stream()
                .map(method -> new InputSocket<>(eventBus,
                        SocketHints.createBooleanSocketHint("Publish " + method.getAnnotation(PublishValue.class).key(), true)))
                .collect(Collectors.toList());
    }

    @Override
    public MapNetworkPublisher<P> createPublisher() {
        return factory.create(keys);
    }

    @Override
    protected void performPublish(S socketValue, MapNetworkPublisher<P> publisher, List<InputSocket<?>> restOfInputSockets) {
        final T value = converter.apply(socketValue);
        // For each PublishValue method in the object being published, put it in the table if the the corresponding
        // checkbox is selected.
        final Map<String, P> publishMap = Maps.newHashMapWithExpectedSize(valueMethods.size());
        try {
            int i = 0;
            for (final Invokable<T, P> method : valueMethods) {
                final String key = method.getAnnotation(PublishValue.class).key();
                final boolean publish = (Boolean) restOfInputSockets.get(i++).getValue().get();

                if (publish) {
                    publishMap.put(key, method.invoke(value));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwables.propagate(e);
        }

        publisher.publish(publishMap);
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
