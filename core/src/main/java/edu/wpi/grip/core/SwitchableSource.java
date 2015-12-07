package edu.wpi.grip.core;


import java.io.IOException;

/**
 * A {@link Source} that can switch its value.
 */
public abstract class SwitchableSource extends Source {

    /**
     * Loads the next value that this source has into the socket.
     * @throws IOException If the source fails to load the next value into the socket.
     */
    public abstract void nextValue() throws IOException;

    /**
     * Loads the previous value that this source has into the socket.
     * @throws IOException If the source fails to load the next value into the socket.
     */
    public abstract void previousValue() throws IOException;

}
