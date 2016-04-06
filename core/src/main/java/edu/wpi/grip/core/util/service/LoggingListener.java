package edu.wpi.grip.core.util.service;


import com.google.common.util.concurrent.Service;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A service listener that will log the service as it transitions between
 * various different states.
 */
@Immutable
public final class LoggingListener extends Service.Listener {
    public final Logger logger;
    public final Class<?> sourceClass;


    public LoggingListener(Logger logger, Class<?> sourceClass) {
        super();
        this.logger = checkNotNull(logger, "Logger cannot be null");
        this.sourceClass = checkNotNull(sourceClass, "Source class cannot be null");
    }

    private String createMessage(String message) {
        return "[" + sourceClass.getSimpleName() + "] " + message;
    }

    @Override
    public void starting() {
        logger.info(createMessage("Starting"));
    }

    @Override
    public void running() {
        logger.fine(createMessage("Running"));
    }

    @Override
    public void stopping(@Nullable Service.State from) {
        logger.fine(createMessage("Stopping from: " + from));
    }

    @Override
    public void terminated(@Nullable Service.State from) {
        logger.info(createMessage("Terminated from: " + from));
    }

    @Override
    public void failed(@Nullable Service.State from, @Nullable Throwable throwable) {
        logger.log(Level.SEVERE, createMessage("Failed from: " + from), throwable);
    }
}
