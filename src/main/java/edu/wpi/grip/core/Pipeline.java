package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import org.bytedeco.javacpp.opencv_core;

import java.util.*;

/**
 * Pipeline has the list of steps in a computer vision algorithm, as well as the set of connections between the inputs
 * and outputs of different steps.
 * <p>
 * The pipeline class is responsible for listening for other components of the application (such as the GUI) adding
 * or removing steps and connections, and for registering and unregistering them from the event bus when appropriate.
 */
public class Pipeline {
    private final EventBus eventBus;
    private final List<Step> steps;
    private final Set<Connection> connections;

    public Pipeline(EventBus eventBus) {
        this.eventBus = eventBus;
        this.steps = new ArrayList<>();
        this.connections = new HashSet<>();

        eventBus.register(this);
    }

    /**
     * Register all steps and connections on the event bus.  This should be called if steps or connections were added
     * other than through events, i.e. by deserializing a pipeline from a file.
     */
    public void register() {
        this.steps.forEach(this.eventBus::register);
        this.connections.forEach(this.eventBus::register);
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

    @Subscribe
    public void onStepAdded(StepAddedEvent event) {
        this.steps.add(event.getIndex().or(this.steps.size()), event.getStep());
        this.eventBus.register(event.getStep());
    }

    @Subscribe
    public void onStepRemoved(StepRemovedEvent event) {
        this.steps.remove(event.getStep());
        this.eventBus.unregister(event.getStep());
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
