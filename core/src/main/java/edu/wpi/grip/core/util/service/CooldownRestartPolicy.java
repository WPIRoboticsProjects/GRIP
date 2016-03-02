package edu.wpi.grip.core.util.service;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Allows you to define a cooldown between failures. Defines whether or not the {@link AutoRestartingService} should
 * restart or not.
 *
 * @see <a href="https://gist.github.com/vladdu/b8af7709e26206b1832b">Original version</a>
 */
public class CooldownRestartPolicy implements ServiceRestartPolicy {
    /**
     * If restarting sooner than this, it's probably an unrecoverable error.
     */
    private static final int DEFAULT_RESTART_INTERVAL = 5;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;


    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private final long interval;
    private final TimeUnit timeUnit;

    public CooldownRestartPolicy() {
        this(DEFAULT_RESTART_INTERVAL, DEFAULT_TIME_UNIT);
    }

    public CooldownRestartPolicy(final long interval, TimeUnit timeUnit) {
        checkArgument(interval >= 0, "The interval must be greater than or equal to zero");
        this.interval = interval;
        this.timeUnit = checkNotNull(timeUnit, "TimeUnit can not be null");
    }

    @Override
    public void notifyRestart() {
        stopwatch.reset();
        stopwatch.start();
    }

    @Override
    public boolean shouldRestart() {
        return stopwatch.elapsed(timeUnit) > interval;
    }

}
