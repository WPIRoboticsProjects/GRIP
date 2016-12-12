package edu.wpi.grip.core.events;

import edu.wpi.grip.core.settings.CodeGenerationSettings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when code generation settings are changed.
 */
public class CodeGenerationSettingsChangedEvent implements DirtiesSaveEvent {

  private final CodeGenerationSettings settings;

  public CodeGenerationSettingsChangedEvent(CodeGenerationSettings settings) {
    this.settings = checkNotNull(settings, "settings");
  }

  public CodeGenerationSettings getCodeGenerationSettings() {
    return settings;
  }

}
