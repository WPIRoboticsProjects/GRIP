package edu.wpi.grip.core.exception;

/**
 * An error that occurs when a checked exception that should never be thrown is thrown. This is <i>not</i> an exception
 * because, if it's thrown, something is wrong with the runtime environment or system that we cannot hope to fix.
 */
public class ImpossibleExceptionError extends Error {

    public ImpossibleExceptionError(String message, Throwable cause) {
        super(message, cause);
    }
}
