package edu.wpi.grip.core.util.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Restartable Service that will automatically restart itself if
 *
 * @see <a href="https://gist.github.com/vladdu/b8af7709e26206b1832b">Original version</a>
 */
public class AutoRestartingService<S extends Service> implements RestartableService {

    private Set<Listener> addedListeners = new HashSet<>();
    private S delegate;
    private final ServiceRestartPolicy policy;
    private final Supplier<S> delegateFactory;
    private final ConcurrentMap<Listener, Executor> listeners;
    private final AtomicBoolean shouldContinueRestarting = new AtomicBoolean(false);

    /**
     * @param factory This should always supply a new instance of the service that can be restarted.
     * @param policy  A policy that allows for custom logic to determine if a service that has entered the
     *                {@link State#FAILED} state should restart.
     */
    public AutoRestartingService(final Supplier<S> factory,
                                 final ServiceRestartPolicy policy) {
        checkNotNull(factory, "The factory can not be null");
        this.policy = checkNotNull(policy, "The policy can not be null");
        this.listeners = Maps.newConcurrentMap();
        this.delegateFactory = () -> {
            // Only add the listeners if we are crating a new instance of the delegate.
            final S newDelegate = factory.get();
            // Clear the list of added listeners because we are creating a new service.
            addedListeners.clear();
            // Now that the listener list is clear we can start adding listeners for real.
            addListenersForReal(newDelegate, addedListeners, new RestartListener(), MoreExecutors.directExecutor());
            this.listeners.entrySet()
                    .forEach(listenerExecutorEntry
                            -> addListenersForReal(newDelegate, addedListeners, listenerExecutorEntry.getKey(), listenerExecutorEntry.getValue()));
            return newDelegate;
        };
        // Create the new delegate using the new factory not the one passed to the constructor.
        this.delegate = this.delegateFactory.get();
    }

    /**
     * This will create an AutoRestartingService that will always restart whenever the existing service
     * enters the {@link State#FAILED} state.
     *
     * @param factory This should always supply a new instance of the service that can be restarted.
     */
    public AutoRestartingService(final Supplier<S> factory) {
        this(factory, () -> true);
    }

    private final class RestartListener extends Listener {

        @Override
        public void failed(final State from, final Throwable failure) {
            if (policy.shouldRestart()) {
                startAsync(false);
            }
        }
    }

    /**
     * Adds the given listener to the service. If the service already contains the given listener it wont be added again.
     *
     * @param service           The service to add the listeners to
     * @param existingListeners The existing set of listeners that have already been added to the service.
     * @param listener          The listener to be added
     * @param executor          The executor to run the listener on.
     */
    private static void addListenersForReal(Service service, Set<Listener> existingListeners, final Listener listener, final Executor executor) {
        if (!existingListeners.contains(listener)) {
            existingListeners.add(listener);
            service.addListener(listener, executor);
        }
    }

    /**
     * Used for testing only.
     *
     * @return delegate
     */
    @VisibleForTesting
    protected S getDelegate() {
        return delegate;
    }

    @Override
    public AutoRestartingService startAsync() {
        shouldContinueRestarting.set(true);
        return startAsync(true);
    }

    /**
     * @param checkIfRunning Check if the service is already running and throw an exception if it is.
     * @return this
     */
    private AutoRestartingService startAsync(boolean checkIfRunning) {
        synchronized (this) {
            if (!shouldContinueRestarting.get()) {
                return this;
            }
            final State currentState = delegate.state();
            if (checkIfRunning && !VALID_START_STATES.contains(currentState)) {
                throw new IllegalStateException("Can not start while in state " + currentState + ". Must be in one of states: " + StringUtils.join(VALID_START_STATES, ", "));
            } else if (!delegate.state().equals(State.NEW)) {
                // If the state of the delegate is NEW then there is no need to create a new one.
                delegate = delegateFactory.get();
            }
            delegate.startAsync();
            policy.notifyRestart();
            return this;
        }
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public State state() {
        return delegate.state();
    }

    @Override
    public synchronized AutoRestartingService stopAsync() {
        shouldContinueRestarting.set(false);
        delegate.stopAsync();
        return this;
    }

    @Override
    public void stopAndAwait(long timeout, TimeUnit unit) throws TimeoutException {
        synchronized (this) {
            shouldContinueRestarting.set(false);
            delegate.stopAsync().awaitTerminated(timeout, unit);
        }
    }

    @Override
    public void stopAndAwait() {
        synchronized (this) {
            shouldContinueRestarting.set(false);
            delegate.stopAsync().awaitTerminated();
        }
    }

    @Override
    public void awaitRunning() {
        delegate.awaitRunning();
    }

    @Override
    public void awaitRunning(final long timeout, final TimeUnit unit)
            throws TimeoutException {
        delegate.awaitRunning(timeout, unit);
    }

    @Override
    public void awaitTerminated() {
        delegate.awaitTerminated();
    }

    @Override
    public void awaitTerminated(final long timeout, final TimeUnit unit)
            throws TimeoutException {
        delegate.awaitTerminated(timeout, unit);
    }

    @Override
    public Throwable failureCause() {
        return delegate.failureCause();
    }

    /**
     * This is only guaranteed to add the listeners to future services.
     * It will make a best effort attempt to add the listener to the existing service.
     *
     * @param listener
     * @param executor
     */
    @Override
    public synchronized void addListener(final Listener listener, final Executor executor) {
        listeners.put(listener, executor);
        addListenersForReal(delegate, addedListeners, listener, executor);
    }

}