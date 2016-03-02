package edu.wpi.grip.core;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import edu.wpi.grip.core.events.*;
import edu.wpi.grip.core.settings.ProjectSettings;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
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

    /*
     * We have separate locks for sources and steps because we don't want to
     * block access to both resources when only one is in use.
     */

    private transient final ReadWriteLock sourceLock = new ReentrantReadWriteLock();
    private final List<Source> sources = new ArrayList<>();
    private transient ReadWriteLock stepLock = new ReentrantReadWriteLock();
    private final List<Step> steps = new ArrayList<>();
    private final Set<Connection> connections = new HashSet<>();
    private ProjectSettings settings = new ProjectSettings();

    /**
     * Remove everything in the pipeline
     */
    public void clear() {
        getSteps().forEach(this::removeStep);
        // We collect the list first because the event modifies the list
        this.sources.stream()
                .map(SourceRemovedEvent::new)
                .collect(Collectors.toList())
                .forEach(this.eventBus::post);
    }

    private final <R> R readSourcesSafely(Function<List<Source>, R> sourceListFunction) {
        return accessSafely(sourceLock.readLock(), Collections.unmodifiableList(sources), sourceListFunction);
    }

    /**
     * Returns a snapshot of all of the sources in the pipeline.
     *
     * @return an Immutable copy of the sources at the current point in the pipeline.
     * @see <a href="https://youtu.be/ZeO_J2OcHYM?t=16m35s">Why we use ImmutableList return type</a>
     */
    public final ImmutableList<Source> getSources() {
        return readSourcesSafely(ImmutableList::copyOf);
    }

    /**
     * @param stepListFunction The function to read the steps with.
     * @param <R>              The return type of the function
     * @return The value returned by the function.
     */
    private final <R> R readStepsSafely(Function<List<Step>, R> stepListFunction) {
        return accessSafely(stepLock.readLock(), Collections.unmodifiableList(steps), stepListFunction);
    }

    /**
     * Returns a snapshot of all of the steps in the pipeline.
     *
     * @return an Immutable copy of the steps at the current point in the pipeline.
     * @see <a href="https://youtu.be/ZeO_J2OcHYM?t=16m35s">Why we use ImmutableList return type</a>
     */
    public final ImmutableList<Step> getSteps() {
        return readStepsSafely(ImmutableList::copyOf);
    }

    /*
     * These methods should not be made public.
     * If you do so you are making a poor design decision and should move whatever you are trying to do into
     * this class.
     */

    /**
     * @param stepListWriterFunction A function that modifies the step list passed to the operation.
     * @param <R>                    The return type of the function
     * @return The value returned by the function.
     */
    private <R> R writeStepsSafely(Function<List<Step>, R> stepListWriterFunction) {
        return accessSafely(stepLock.writeLock(), steps, stepListWriterFunction);
    }

    /**
     * @param stepListWriterConsumer A consumer that can modify the list that is passed to it.
     */
    private void writeStepsSafelyConsume(Consumer<List<Step>> stepListWriterConsumer) {
        writeStepsSafely(stepList -> {
            stepListWriterConsumer.accept(stepList);
            return null;
        });
    }

    private <R> R writeSourcesSafely(Function<List<Source>, R> sourceListWriterFunction) {
        return accessSafely(sourceLock.writeLock(), sources, sourceListWriterFunction);
    }

    private void writeSourcesSafelyConsume(Consumer<List<Source>> sourceListWriterFunction) {
        writeSourcesSafely(sources -> {
            sourceListWriterFunction.accept(sources);
            return null;
        });
    }

    /*
     * End of methods that should not be made public
     */

    /**
     * Locks the resource with the specified lock and performs the function.
     * When the function is complete then the lock unlocked again.
     *
     * @param lock         The lock for the given resource
     * @param list         The list that will be accessed while the resource is locked
     * @param listFunction The function that either modifies or accesses the list
     * @param <T>          The type of list
     * @param <R>          The return value for the function
     * @return The value returned by the list function
     */
    private static <T, R> R accessSafely(Lock lock, List<T> list, Function<List<T>, R> listFunction) {
        final R returnValue;
        lock.lock();
        try {
            returnValue = listFunction.apply(list);
        } finally {
            // Ensure that no matter what may get thrown while reading the steps we unlock
            lock.unlock();
        }
        return returnValue;
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
    private boolean isBefore(Step step1, Step step2) {
        return readStepsSafely(steps -> steps.indexOf(step1) < steps.indexOf(step2));
    }

    @Subscribe
    public void onSourceAdded(SourceAddedEvent event) {
        writeSourcesSafelyConsume(sources -> {
            sources.add(event.getSource());
        });
    }

    @Subscribe
    public void onSourceRemoved(SourceRemovedEvent event) {
        writeSourcesSafelyConsume(sources -> {
            sources.remove(event.getSource());
        });

        // Sockets of deleted sources should not be previewed
        for (OutputSocket<?> socket : event.getSource().getOutputSockets()) {
            socket.setPreviewed(false);
        }
    }

    public void addStep(int index, Step step) {
        checkNotNull(step, "The step can not be null");
        checkArgument(!step.removed(), "The step must not have been disabled already");

        writeStepsSafelyConsume(steps -> steps.add(index, step));

        this.eventBus.register(step);
        this.eventBus.post(new StepAddedEvent(step, index));
    }

    public void addStep(Step step) {
        addStep(this.steps.size(), step);
    }

    public void removeStep(Step step) {
        checkNotNull(step, "The step can not be null");

        writeStepsSafelyConsume(steps -> steps.remove(step));

        // Sockets of deleted steps should not be previewed
        for (OutputSocket<?> socket : step.getOutputSockets()) {
            socket.setPreviewed(false);
        }
        step.setRemoved();
        this.eventBus.unregister(step);
        this.eventBus.post(new StepRemovedEvent(step));
    }

    public synchronized void moveStep(Step step, int delta) {
        checkNotNull(step, "The step can not be null");
        checkArgument(this.steps.contains(step), "The step must exist in the pipeline to be moved");

        // We are modifying the steps array
        writeStepsSafelyConsume(steps -> {
            final int oldIndex = this.steps.indexOf(step);
            this.steps.remove(oldIndex);

            // Compute the new index of the step, clamping to the beginning or end of pipeline if it goes past either end
            final int newIndex = Math.min(Math.max(oldIndex + delta, 0), this.steps.size());
            this.steps.add(newIndex, step);
        });

        // Do not lock while posting the event
        eventBus.post(new StepMovedEvent(step, delta));

    }

    @Subscribe
    public void onConnectionAdded(ConnectionAddedEvent event) {
        final Connection connection = event.getConnection();
        this.connections.add(connection);
        this.eventBus.register(connection);
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
