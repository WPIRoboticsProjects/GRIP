package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.events.CodeGenerationSettingsChangedEvent;
import edu.wpi.grip.core.settings.CodeGenerationSettings;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * XStream converter for {@link edu.wpi.grip.core.settings.CodeGenerationSettings}.
 */
public class CodeGenerationSettingsConverter extends ReflectionConverter {

  private static final Logger logger =
      Logger.getLogger(CodeGenerationSettingsConverter.class.getName());

  private final EventBus eventBus;

  @Inject
  public CodeGenerationSettingsConverter(Project project, EventBus eventBus) {
    super(project.xstream.getMapper(),
        project.xstream.getReflectionProvider(),
        CodeGenerationSettings.class);
    this.eventBus = eventBus;
  }

  @Override
  public CodeGenerationSettings unmarshal(HierarchicalStreamReader reader,
                                          UnmarshallingContext context) {
    CodeGenerationSettings settings = (CodeGenerationSettings) super.unmarshal(reader, context);
    if (!Files.isDirectory(Paths.get(settings.getSaveDir()))) {
      logger.warning("Save dir '" + settings.getSaveDir() + "' does not exist; using default");
      settings = CodeGenerationSettings.builder(settings)
          .saveDir(CodeGenerationSettings.DEFAULT_SETTINGS.getSaveDir())
          .build();
    }
    eventBus.post(new CodeGenerationSettingsChangedEvent(settings));
    return settings;
  }

}
