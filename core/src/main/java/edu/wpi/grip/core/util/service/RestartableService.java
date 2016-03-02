package edu.wpi.grip.core.util.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This interface defines a {@link Service} that breaks the guarantee that
 * the {@link State#FAILED} and {@link State#TERMINATED} states are terminal states.
 * <p>
 * Instead, as long as the service is in a non running state the service can be started.
 */
public interface RestartableService extends Service {
    ImmutableSet<State> VALID_START_STATES =
            ImmutableSet.<State>builder().add(State.NEW, State.FAILED, State.TERMINATED).build();

    /*
     * We override the startAsync and stopAsync to ensure that the service returned
     * does not try to enforce the API guaranteed by service.
     */

    /**
     * If the service state is {@link State#NEW}, {@link State#FAILED}, {@link State#TERMINATED}
     * this initiates service startup and returns
     * immediately.
     *
     * @return this
     * @throws IllegalStateException if the service is not in one of {@link #VALID_START_STATES}
     */
    @Override
    RestartableService startAsync();


    /**
     *
     * @return
     */
    @Override
    RestartableService stopAsync();

    void stopAndAwait();

    void stopAndAwait(long timeout, TimeUnit unit) throws TimeoutException;

}
