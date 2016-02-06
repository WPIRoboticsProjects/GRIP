package edu.wpi.grip.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * @see {@link com.google.common.testing.ClusterException} for the origin of this
 */
public final class MultiException extends RuntimeException {

    private MultiException(Collection<? extends Throwable> exceptions) {
        this(exceptions.size(), exceptions.iterator());
    }

    private MultiException(int size, Iterator<? extends Throwable> exceptions) {
        super(size + " exceptions were thrown. The first exception is listed as a cause.", exceptions.next());
        exceptions.forEachRemaining(this::addSuppressed);
    }

    /**
     * Given a collection of exceptions, returns a {@link RuntimeException}, with
     * the following rules:
     *
     * <ul>
     *  <li>If {@code exceptions} has a single exception and that exception is a
     *    {@link RuntimeException}, return it
     *  <li>If {@code exceptions} has a single exceptions and that exceptions is
     *    <em>not</em> a {@link RuntimeException}, return a simple
     *    {@code RuntimeException} that wraps it
     *  <li>Otherwise, return an instance of {@link MultiException} that wraps
     *    the first exception in the {@code exceptions} collection.
     * </ul>
     *
     * <p>Though this method takes any {@link Collection}, it often makes most
     * sense to pass a {@link java.util.List} or some other collection that
     * preserves the order in which the exceptions got added.
     *
     * @throws NullPointerException if {@code exceptions} is null
     * @throws IllegalArgumentException if {@code exceptions} is empty
     */
    public static RuntimeException create(Collection<? extends Throwable> exceptions) {
        if (exceptions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Can't create an ExceptionCollection with no exceptions");
        }
        if (exceptions.size() == 1) {
            Throwable temp = exceptions.iterator().next();
            if (temp instanceof RuntimeException) {
                return (RuntimeException)temp;
            } else {
                return new RuntimeException(temp);
            }
        }
        return new MultiException(exceptions);
    }

}
