package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;

import javax.inject.Inject;

/**
 * XStream converter for {@link ProjectSettings}.
 * <p>
 * Settings are serialized using XStream's built-in reflection serialization. The only catch is that a
 * {@link ProjectSettingsChangedEvent} must be posted after new settings are loaded.
 */
public class ProjectSettingsConverter extends ReflectionConverter {

    @Inject private EventBus eventBus;

    @Inject
    public ProjectSettingsConverter(Project project) {
        super(project.xstream.getMapper(), project.xstream.getReflectionProvider(), ProjectSettings.class);
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        ProjectSettings settings = (ProjectSettings) super.unmarshal(reader, context);
        eventBus.post(new ProjectSettingsChangedEvent(settings));
        return null;
    }
}
