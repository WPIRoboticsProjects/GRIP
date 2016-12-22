package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.events.CodeGenerationSettingsChangedEvent;
import edu.wpi.grip.core.settings.CodeGenerationSettings;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * XStream converter for {@link edu.wpi.grip.core.settings.CodeGenerationSettings}.
 */
public class CodeGenerationSettingsConverter extends ReflectionConverter {

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
    eventBus.post(new CodeGenerationSettingsChangedEvent(settings));
    return settings;
  }

}
