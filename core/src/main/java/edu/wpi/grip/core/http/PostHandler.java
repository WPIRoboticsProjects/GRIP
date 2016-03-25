
package edu.wpi.grip.core.http;

/**
 * Handler for converting incoming data over an HTTP {@code POST} operation.
 */
public interface PostHandler {

    /**
     * Converts the given bytes to some object. This operation should have side
     * effects (i.e. it is <i>stateful</i>}.
     *
     * @param bytes the bytes to convert
     * @return true if the conversion was a success; false if not.
     */
    boolean convert(byte[] bytes);

}
