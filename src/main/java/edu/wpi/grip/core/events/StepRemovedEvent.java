package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Step;

import java.util.Optional;

/**
 * An event that occurs when a new step is removed from the pipeline.  This is triggered by the user deleting a step
 * from the GUI.
 */
public class StepRemovedEvent implements RemovedEvent {
    private final Step step;
    private final Optional<Integer> index;

    /**
     * @param step The step being deleted
     */
    public StepRemovedEvent(Step step, final int index) {
        this.step = step;
        this.index = Optional.of(index);
    }

    /**
     *
     */
    public StepRemovedEvent(Step step) {
        this.step = step;
        this.index = Optional.empty();
    }

    /**
     * @return The step being deleted.
     */
    public Step getStep() {
        return this.step;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("step", step)
                .toString();
    }

    @Override
    public StepAddedEvent createUndoEvent() {
        if( index.isPresent() ){
            return new StepAddedEvent(step, index.get());
        } else {
            return new StepAddedEvent(step);
        }
    }
}
