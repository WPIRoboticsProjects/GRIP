package edu.wpi.grip.core.events;


/**
 * Any event that indicates to the {@link edu.wpi.grip.core.PipelineRunner} that it should run.
 */
public interface RunPipelineEvent {

    /**
     * Indicates to the {@link edu.wpi.grip.core.PipelineRunner} that there is an update to one of the values and it should run again.
     * @return true if the {@link edu.wpi.grip.core.PipelineRunner#pipelineFlag} should be released to run because of the update.
     */
    default boolean pipelineShouldRun() {
        return true;
    }
}
