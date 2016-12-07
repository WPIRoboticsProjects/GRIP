package edu.wpi.grip.core.events;

/**
 * An event representing the start of a single benchmarked pipeline run.
 */
public class StartSingleBenchmarkRunEvent implements RunPipelineEvent {

  @Override
  public boolean pipelineShouldRun() {
    return true;
  }
}
