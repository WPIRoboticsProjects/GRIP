package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.core.operations.networktables.NTManager;
import edu.wpi.grip.core.settings.ProjectSettings;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

    @Inject
    @XStreamOmitField
    private EventBus eventBus;

    @Inject
    @XStreamOmitField
    private NTManager ntManager;

    private final List<Source> sources = new ArrayList<>();
    private final List<Step> steps = new ArrayList<>();
    private final Set<Connection> connections = new HashSet<>();
    private ProjectSettings settings = new ProjectSettings();

    /**
     * Remove everything in the pipeline
     */
    public void clear() {
        this.steps.stream().collect(Collectors.toList()).forEach(this::removeStep);

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

    /*
     * @return The current per-project settings.  This object may become out of date if the settings are edited
     * by the user, so objects requiring a preference value should also subscribe to {@link ProjectSettingsChangedEvent}
     * to get updates.
     */
    public ProjectSettings getProjectSettings() {
        return settings;
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

    public synchronized void addStep(int index, Step step) {
        checkNotNull(step, "The step can not be null");
        this.steps.add(index, step);
        this.eventBus.register(step);
        this.eventBus.post(new StepAddedEvent(step, index));
    }

    public synchronized void addStep(Step step) {
        addStep(this.steps.size(), step);
    }

    public synchronized void removeStep(Step step) {
        checkNotNull(step, "The step can not be null");
        this.steps.remove(step);
        // Sockets of deleted steps should not be previewed
        for (OutputSocket<?> socket : step.getOutputSockets()) {
            socket.setPreviewed(false);
        }
        this.eventBus.unregister(step);
        this.eventBus.post(new StepRemovedEvent(step));
    }

    public synchronized void moveStep(Step step, int delta) {
        checkNotNull(step, "The step can not be null");
        checkArgument(this.steps.contains(step), "The step must exist in the pipeline to be moved");

        final int oldIndex = this.steps.indexOf(step);
        this.steps.remove(oldIndex);

        // Compute the new index of the step, clamping to the beginning or end of pipeline if it goes past either end
        final int newIndex = Math.min(Math.max(oldIndex + delta, 0), this.steps.size());
        this.steps.add(newIndex, step);
        eventBus.post(new StepMovedEvent(step, delta));
    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        final Connection connection = event.getConnection();
        this.connections.add(connection);
    }

    @Subscribe
    public void onConnectionRemoved(ConnectionRemovedEvent event) {
        this.connections.remove(event.getConnection());
        this.eventBus.unregister(event.getConnection());
    }

    @Subscribe
    public void onProjectSettingsChanged(ProjectSettingsChangedEvent event) {
        this.settings = event.getProjectSettings();
    }
}
