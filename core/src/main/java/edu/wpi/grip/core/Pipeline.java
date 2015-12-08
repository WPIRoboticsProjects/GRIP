package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import edu.wpi.grip.core.events.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pipeline has the list of steps in a computer vision algorithm, as well as the set of connections between the inputs
 * and outputs of different steps.
 * <p>
 * The pipeline class is responsible for listening for other components of the application (such as the GUI) adding
 * or removing steps and connections, and for registering and unregistering them from the event bus when appropriate.
 */
@XStreamAlias(value = "grip:Pipeline")
public class Pipeline {

    @XStreamOmitField
    private final EventBus eventBus;

    private final List<Source> sources;
    private final List<Step> steps;
    private final Set<Connection> connections;

    public Pipeline(EventBus eventBus) {
        this.eventBus = eventBus;
        this.sources = new ArrayList<>();
        this.steps = new ArrayList<>();
        this.connections = new HashSet<>();

        eventBus.register(this);
    }

    /**
     * Register all of the objects composing the pipeline on the event bus.  This should be called if steps,
     * connections, etc. were added other than through events, i.e. by deserializing a pipeline from a file.
     */
    public void register() {
        this.steps.forEach(this.eventBus::register);
        this.connections.forEach(this.eventBus::register);
    }

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
     * @return true if the step1 is before step2 in the pipeline
     */
    protected synchronized boolean isBefore(Step step1, Step step2) {
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
        step.setPipeline(this);

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
