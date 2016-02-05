package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that occurs when the value stored in a socket changes.  This can happen, for example, as the result of an
 * operation completing, or as a response to user input.
 */
public class SocketChangedEvent implements RunPipelineEvent {
    private final Socket socket;

    /**
     * @param socket The socket that changed, with its new value.
     */
    public SocketChangedEvent(Socket socket) {
        this.socket = checkNotNull(socket, "Socket can not be null");
    }


    /**
     * @return The socket that changed, with its new value.
     */
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public boolean pipelineShouldRun() {
        /*
         * The pipeline should only flag an update when it is an input changing. A changed output doesn't mean
         * the pipeline is dirty.
         * If the socket is connected to another socket then then the input will only change because of the
         * pipeline thread.
         * In that case we don't want the pipeline to be releasing itself.
         * If the connections are empty then the change must have come from the UI so we need to run the pipeline
         * with the new values.
         */
        return getSocket().getDirection().equals(Socket.Direction.INPUT) && getSocket().getConnections().isEmpty();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("socket", socket)
                .toString();
    }
}
