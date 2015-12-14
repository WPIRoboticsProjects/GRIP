package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import edu.wpi.grip.core.events.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline has the list of steps in a computer vision algorithm, as well as the set of connections between the inputs
 * and outputs of different steps.
 * <p>
 * The pipeline class is responsible for listening for other components of the application (such as the GUI) adding
 * or removing steps and connections, and for registering and unregistering them from the event bus when appropriate.
 */
@Singleton
@XStreamAlias(value = "grip:Pipeline")
public class Pipeline {

    @Inject @XStreamOmitField private EventBus eventBus;

    private List<Source> sources = new ArrayList<>();
    private List<Step> steps = new ArrayList<>();
    private Set<Connection> connections = new HashSet<>();

    /**
     * Remove everything in the pipeline
     */
    public void clear() {
        // These streams are both collected into lists because streams cannot modify their source.  Sending a
        // StepRemovedEvent or SourceRemovedEvent modifies this.steps or this.sources.
        this.steps.stream()
                .map(StepRemovedEvent::new)
                .collect(Collectors.toList())
                .forEach(this.eventBus::post);

        this.sources.stream()
                .map(SourceRemovedEvent::new)
                .collect(Collectors.toList())
                .forEach(this.eventBus::post);
    }

    /**
     * @return The unmodifiable list of sources for inputs to the algorithm
     * @see Source
     */
    public List<Source> getSources() {
        return Collections.unmodifiableList(this.sources);
    }

    /**
     * @return The unmodifiable list of steps in the computer vision algorithm
     */
    public List<Step> getSteps() {
        return Collections.unmodifiableList(this.steps);
    }

    /**
     * @return The unmodifiable set of connections between inputs and outputs of steps in the algorithm
     */
    public Set<Connection> getConnections() {
        return Collections.unmodifiableSet(this.connections);
    }

    /**
     * @return true if a connection can be made from the given output socket to the given input socket
     */
    @SuppressWarnings("unchecked")
    public boolean canConnect(Socket socket1, Socket socket2) {
        final OutputSocket<?> outputSocket;
        final InputSocket<?> inputSocket;

        // One socket must be an input and one must be an output
        if (socket1.getDirection() == socket2.getDirection()) {
            return false;
        }

        if (socket1.getDirection().equals(Socket.Direction.OUTPUT)) {
            outputSocket = (OutputSocket) socket1;
            inputSocket = (InputSocket) socket2;
        } else {
            inputSocket = (InputSocket) socket1;
            outputSocket = (OutputSocket) socket2;
        }

        final SocketHint outputHint = socket1.getSocketHint();
        final SocketHint inputHint = socket2.getSocketHint();

        // The input socket must be able to hold the type of value that the output socket contains
        if (!inputHint.getType().isAssignableFrom(outputHint.getType())) {
            return false;
        }

        // Input sockets can only be connected to one thing
        if (!inputSocket.getConnections().isEmpty()) {
            return false;
        }

        // If both sockets are in steps, the output must be before the input in the pipeline.  This prevents "backwards"
        // connections, which both enforces a well-organized pipeline and prevents feedback loops.
        final boolean[] backwards = {false};
        outputSocket.getStep().ifPresent(outputStep -> inputSocket.getStep().ifPresent(inputStep -> {
            if (!isBefore(outputStep, inputStep)) {
                backwards[0] = true;
            }
        }));

        return !backwards[0];
    }

    /**
     * @return true if the step1 is before step2 in the pipeline
     */
    private synchronized boolean isBefore(Step step1, Step step2) {
        return this.steps.indexOf(step1) < this.steps.indexOf(step2);
    }

    @Subscribe
    public void onSourceAdded(SourceAddedEvent event) {
        this.sources.add(event.getSource());
    }

    @Subscribe
    public void onSourceRemoved(SourceRemovedEvent event) {
        this.sources.remove(event.getSource());

        // Sockets of deleted sources should not be previewed
        for (OutputSocket<?> socket : event.getSource().getOutputSockets()) {
            socket.setPreviewed(false);
        }
    }

    @Subscribe
    public synchronized void onStepAdded(StepAddedEvent event) {
        final Step step = event.getStep();

        this.steps.add(event.getIndex().or(this.steps.size()), step);
        this.eventBus.register(event.getStep());
    }

    @Subscribe
    public synchronized void onStepRemoved(StepRemovedEvent event) {
        this.steps.remove(event.getStep());
        this.eventBus.unregister(event.getStep());

        // Sockets of deleted steps should not be previewed
        for (OutputSocket<?> socket : event.getStep().getOutputSockets()) {
            socket.setPreviewed(false);
        }
    }

    @Subscribe
    public synchronized void onStepMoved(StepMovedEvent event) {
        final Step step = event.getStep();

        final int oldIndex = this.steps.indexOf(step);
        this.steps.remove(oldIndex);

        // Compute the new index of the step, clamping to the beginning or end of pipeline if it goes past either end
        final int newIndex = Math.min(Math.max(oldIndex + event.getDistance(), 0), this.steps.size());
        this.steps.add(newIndex, step);
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        this.connections.add(event.getConnection());
        this.eventBus.register(event.getConnection());
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        this.connections.remove(event.getConnection());
        this.eventBus.unregister(event.getConnection());
    }


}
