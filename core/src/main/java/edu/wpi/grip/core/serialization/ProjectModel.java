package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.Pipeline;

import com.github.zafarkhaja.semver.Version;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Data model class for saving and loading projects from save files.
 */
@XStreamAlias("grip:Project")
public class ProjectModel {

  private Pipeline pipeline;
  private Version version;

  /**
   * Only used for XStream deserialization.
   */
  public ProjectModel() {
    this(null, null);
  }

  public ProjectModel(Pipeline pipeline, Version version) {
    this.pipeline = pipeline;
    this.version = version;
  }

  public Pipeline getPipeline() {
    return pipeline;
  }

  public Version getVersion() {
    return version;
  }

}
