package edu.wpi.grip.core.events;

/**
 * An event representing the start of benchmarked pipeline run.
 */
public class BenchmarkRunEvent implements RunPipelineEvent {

  @Override
  public boolean pipelineShouldRun() {
    return true;
  }
}
