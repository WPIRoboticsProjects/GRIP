package edu.wpi.grip.core.events;

import com.google.common.base.MoreObjects;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.Step;

/**
 * An event that occurs when a new step is added to the pipeline.  This is triggered by the user adding a step with the
 * GUI.
 */
public class StepAddedEvent {
    private Step step;
    private int index;
    private boolean addAtEnd;

    /**
     * @param step  The step being added
     * @param index The index that the step should be added at
     */
    public StepAddedEvent(Step step, int index) {
        this.step = step;
        this.addAtEnd = false;
        this.index = index;
    }

    /**
     * @param step The step being added to the end of the pipeline
     */
    public StepAddedEvent(Step step) {
        this.step = step;
        this.addAtEnd = true;
    }

    /**
     * @return The step being added.
     */
    public Step getStep() {
        return this.step;
    }

    /**
     * @return <code>true</code> if the step should be added at the end of the pipeline.  Otherwise, {@link #getIndex()}
     * returns the index it should be added at.
     */
    public boolean getAddAtEnd() {
        return this.addAtEnd;
    }

    /**
     * @return The index that the step should be added at, assuming {@link #getAddAtEnd()} is <code>true</code>
     */
    public int getIndex() {
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
