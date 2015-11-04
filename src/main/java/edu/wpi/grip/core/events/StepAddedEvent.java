package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Step;

import java.util.Optional;


/**
 * An event that occurs when a new step is added to the pipeline.  This is triggered by the user adding a step with the
 * GUI.
 */
public class StepAddedEvent implements AddedEvent {
    private final Step step;
    private final Optional<Integer> index;

    /**
     * @param step  The step being added
     * @param index The index that the step should be added at.
     */
    public StepAddedEvent(Step step, int index) {
        this.step = step;
        this.index = Optional.of(index);
    }

    /**
     * @param step The step being added to the end of the pipeline
     */
    public StepAddedEvent(Step step) {
        this.step = step;
        this.index = Optional.empty();
    }

    /**
     * @return The step being added.
     */
    public Step getStep() {
        return this.step;
    }

    /**
     * @return The index that the step should be added at, unless the step should be added at the end.
     */
    public Optional<Integer> getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("step", step)
                .add("index", index)
                .toString();
    }

    @Override
    public StepRemovedEvent createUndoEvent() {
        if (index.isPresent()){
            return new StepRemovedEvent(step, index.get());
        } else {
            return new StepRemovedEvent(step);
        }
    }
}
