package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.SocketHints;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Publishes data to a specific network protocol.
 * <p>
 * This looks at {@link PublishValue} annotations on accessor methods in a class to generate the data to publish.
 */
public abstract class PublishAnnotatedOperation<D, P extends Publishable> extends NetworkPublishOperation<D> {

    private final InputSocket.Factory isf;
    private final Class<P> publishType;
    private final Function<D, P> converter;
    private final MapNetworkPublisher publisher;

    protected PublishAnnotatedOperation(InputSocket.Factory isf,
                                        Class<D> dataType,
                                        Class<P> publishType,
                                        Function<D, P> converter,
                                        MapNetworkPublisherFactory publisherFactory) {
        super(isf, dataType);
        checkNotNull(publishType);
        checkNotNull(converter);
        checkNotNull(publisherFactory);

        if (!Modifier.isPublic(publishType.getModifiers())) {
            throw new IllegalAccessError("Cannot access methods in non-public Publishable class");
        }

        if (!Modifier.isStatic(publishType.getModifiers()) && publishType.isMemberClass()) {
            throw new IllegalAccessError("Cannot access methods in non-static inner class");
        }

        this.isf = isf;
        this.publishType = publishType;
        this.converter = converter;
        this.publisher = publisherFactory.create(valueMethodStream()
                .map(m -> m.getAnnotation(PublishValue.class).key())
                .filter(k -> !k.isEmpty())
                .collect(Collectors.toSet()));

        // Make sure there's at least one method to call
        valueMethodStream()
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("A Publishable type must have at least one method annotated with @PublishValue"));

        // Make sure keys and weights are distinct
        valueMethodStream()
                .map(m -> m.getAnnotation(PublishValue.class))
                .filter(a -> valueMethodStream()
                        .map(m -> m.getAnnotation(PublishValue.class))
                        .anyMatch(a0 -> !a.equals(a0) && (a.key().equals(a0.key()) || a.weight() == a0.weight())))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalArgumentException("Keys and weights must be distinct");
                });

        // Make sure all methods are non-static
        valueMethodStream()
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalArgumentException("Methods annotated with @PublishValue must be non-static");
                });

        // Make sure all methods are public
        valueMethodStream()
                .filter(m -> !Modifier.isPublic(m.getModifiers()))
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalArgumentException("Methods annotated with @PublishValue must be public");
                });

        // Make sure annotated methods don't take parameters
        valueMethodStream()
                .filter(m -> m.getParameterCount() > 0)
                .findAny()
                .ifPresent(x -> {
                    throw new IllegalArgumentException("Methods annotated with @PublishValue cannot take parameters");
                });

        // Make sure all methods have keys
        if (valueMethodStream()
                .map(m -> m.getAnnotation(PublishValue.class))
                .filter(a -> a.key().isEmpty())
                .count() > 0
                &&
                valueMethodStream()
                        .map(m -> m.getAnnotation(PublishValue.class))
                        .filter(a -> !a.key().isEmpty())
                        .count() > 0) {
            throw new IllegalArgumentException("If a method has no key, it can be the only one annotated with @PublishValue in the class");
        }
    }

    /**
     * Gets a stream of all valid methods annotated with {@link PublishValue} in the class of the data to publish.
     * The methods are sorted by weight.
     */
    protected Stream<Method> valueMethodStream() {
        return Stream.of(publishType.getMethods())
                .filter(m -> m.isAnnotationPresent(PublishValue.class))
                .sorted(Comparator.comparing(m -> m.getAnnotation(PublishValue.class).weight()));
    }

    @Override
    protected List<InputSocket<Boolean>> createFlagSockets() {
        return valueMethodStream()
                .map(m -> m.getAnnotation(PublishValue.class).key())
                .map(name -> SocketHints.createBooleanSocketHint("Publish " + name, true))
                .map(isf::create)
                .collect(Collectors.toList());
    }

    @Override
    protected void doPublish() {
        publisher.setName(nameSocket.getValue().get());
        D data = dataSocket.getValue().get();
        Map<String, Object> dataMap = valueMethodStream()
                .map(m -> Pair.of(m.getAnnotation(PublishValue.class).key(), get(m, converter.apply(data))))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        publisher.publish(dataMap);
    }

    public Class<D> getSocketType() {
        return dataType;
    }

    /**
     * Helper method for invoking an accessor method on an object.
     *
     * @param accessor the accessor method to invoke
     * @param instance the object to invoke the accessor on
     * @return the value returned by the accessor method, or {@code null} if the method could not be invoked.
     */
    protected Object get(Method accessor, Object instance) {
        try {
            return accessor.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

}
