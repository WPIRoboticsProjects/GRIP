package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.events.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A connection is a rule that causes one socket to update to always the value of another socket.
 */
@XStreamAlias(value = "grip:Connection")
public class Connection<T> {

    private final EventBus eventBus;
    private final OutputSocket<? extends T> outputSocket;
    private final InputSocket<T> inputSocket;


    public interface Factory <T> {
        Connection<T> create(OutputSocket<? extends T> outputSocket, InputSocket<T> inputSocket);
    }

    /**
     * @param pipeline     The pipeline to create the connection inside of.
     * @param outputSocket The socket to listen for changes in.
     * @param inputSocket  A different socket to update when a change occurs in the first.
     */
    @Inject
    Connection(EventBus eventBus, Pipeline pipeline, @Assisted OutputSocket<? extends T> outputSocket, @Assisted InputSocket<T> inputSocket) {
        this.eventBus = eventBus;
        this.outputSocket = outputSocket;
        this.inputSocket = inputSocket;
        checkArgument(pipeline.canConnect(outputSocket, inputSocket), "Cannot connect sockets");
    }

    public OutputSocket<? extends T> getOutputSocket() {
        return this.outputSocket;
    }

    public InputSocket<T> getInputSocket() {
        return this.inputSocket;
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        if (event.getConnection().equals(this)) {
            inputSocket.addConnection(this);
            outputSocket.addConnection(this);
            inputSocket.setValueOptional(outputSocket.getValue());
        }
    }

    @Subscribe
    public void onOutputChanged(SocketChangedEvent e) {
        if (e.getSocket() == outputSocket) {
            inputSocket.setValueOptional(outputSocket.getValue());
        }
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent e) {
        if (e.getConnection() == this) {
            inputSocket.removeConnection(this);
            outputSocket.removeConnection(this);
        }
    }

    @Subscribe
    public void removeConnection(StepRemovedEvent e) {
        // Remove this connection if one of the steps it was connected to was removed
        for (OutputSocket socket : e.getStep().getOutputSockets()) {
            if (socket == this.outputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }

        for (InputSocket socket : e.getStep().getInputSockets()) {
            if (socket == this.inputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }
    }

    @Subscribe
    public void removeConnection(SourceRemovedEvent e) {
        // Remove this connection if it's from a source that was removed
        for (OutputSocket socket : e.getSource().getOutputSockets()) {
            if (socket == this.outputSocket) {
                this.eventBus.post(new ConnectionRemovedEvent(this));
                return;
            }
        }
    }
}
