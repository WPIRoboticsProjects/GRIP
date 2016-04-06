package edu.wpi.grip.core.util.service;

import com.google.common.util.concurrent.Service;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Will run the same runnable regardless of what listener callback is being called.
 */
public class SingleActionListener extends Service.Listener {
    private final Runnable runnable;

    public SingleActionListener(Runnable runnable) {
        super();
        this.runnable = checkNotNull(runnable, "Runnable can not be null");
    }

    @Override
    public final void starting() {
        runnable.run();
    }

    @Override
    public final void running() {
        runnable.run();
    }

    @Override
    public final void stopping(@Nullable Service.State from) {
        runnable.run();
    }

    @Override
    public final void terminated(@Nullable Service.State from) {
        runnable.run();
    }

    @Override
    public final void failed(@Nullable Service.State from, @Nullable Throwable failure) {
        runnable.run();
    }
}
