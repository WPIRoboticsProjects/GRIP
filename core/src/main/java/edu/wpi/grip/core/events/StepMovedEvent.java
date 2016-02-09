package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Step;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that occurs when a new step is moved from one position to another in the pipeline
 */
public class StepMovedEvent {
    private final Step step;
    private final int distance;

    /**
     * @param step     The step being moved
     * @param distance The number of indices (positive or negative) to move the step by
     */
    public StepMovedEvent(Step step, int distance) {
        this.step = checkNotNull(step, "Step cannot be null");
        this.distance = distance;
    }

    public Step getStep() {
        return this.step;
    }

    public int getDistance() {
        return this.distance;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("step", step)
                .add("distance", distance)
                .toString();
    }
}
