package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.StepAddedEvent;
import edu.wpi.grip.core.events.StepRemovedEvent;
import org.bytedeco.javacpp.opencv_core;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pipeline has the list of steps in a computer vision algorithm, as well as the set of connections between the inputs
 * and outputs of different steps.
 *
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
     * @return The list of steps in the computer vision algorithm
     */
    public List<Step> getSteps() {
        return this.steps;
    }

    /**
     * @return The list of connections between inputs and outputs of steps in the algorithm
     */
    public Set<Connection> getConnections() {
        return this.connections;
    }

    @Subscribe
    public void onStepAdded(StepAddedEvent event) {
        if (event.getAddAtEnd()) {
            this.steps.add(this.steps.size(), event.getStep());
        } else {
            this.steps.add(event.getIndex(), event.getStep());
        }

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
