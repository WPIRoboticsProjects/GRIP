package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Step;

import javax.annotation.Nonnegative;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * An event that occurs when a new step is added to the pipeline.  This is triggered by the user adding a step with the
 * GUI.
 */
public class StepAddedEvent {
    private final Step step;
    private final OptionalInt index;

    /**
     * @param step  The step being added
     * @param index The index that the step should be added at.
     */
    public StepAddedEvent(Step step, @Nonnegative int index) {
        this.step = checkNotNull(step, "Step can not be null");
        this.index = OptionalInt.of(index);
    }

    /**
     * @param step The step being added to the end of the pipeline
     */
    public StepAddedEvent(Step step) {
        this.step = checkNotNull(step, "Step can not be null");
        this.index = OptionalInt.empty();
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
    public OptionalInt getIndex() {
        return this.index;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("step", step)
                .add("index", index)
                .toString();
    }
}
