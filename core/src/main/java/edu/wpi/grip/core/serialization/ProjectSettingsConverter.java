package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import edu.wpi.grip.core.ProjectSettings;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;

/**
 * XStream converter for {@link ProjectSettings}.
 * <p>
 * Settings are serialized using XStream's built-in JavaBean serialization. The only catch is that a
 * {@link ProjectSettingsChangedEvent} must be posted after new settings are loaded.
 */
public class ProjectSettingsConverter extends JavaBeanConverter {

    private final EventBus eventBus;

    public ProjectSettingsConverter(Mapper mapper, EventBus eventBus) {
        super(mapper, ProjectSettings.class);
        this.eventBus = eventBus;
    }

    @Override
    public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
        ProjectSettings settings = (ProjectSettings) super.unmarshal(reader, context);
        eventBus.post(new ProjectSettingsChangedEvent(settings));
        return null;
    }
}
