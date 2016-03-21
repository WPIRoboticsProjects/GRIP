
package edu.wpi.grip.core.exception;

/**
 * An exception thrown when something goes wrong with an internal GRIP
 * operation.
 */
public class GripException extends RuntimeException {

    public GripException() {
        super();
    }

    public GripException(String message) {
        super(message);
    }

    public GripException(Throwable cause) {
        super(cause);
    }

    public GripException(String message, Throwable cause) {
        super(message, cause);
    }

}
