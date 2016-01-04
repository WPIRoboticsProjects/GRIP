package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceAddedEvent;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

/**
 * XStream converter for sources.
 * <p>
 * Sources typically consist of some static configuration (like a device number, URL, or file path), as well as some
 * sort of connection.  Since we only need to serialize this static state and then later use it to set the state of a
 * new instance, sources are simply serialized by saving the result of {@link Source#getProperties()}.
 * <p>
 * To deserialize a source, we create a new instance of the appropriate class and then call
 * {@link edu.wpi.grip.core.Source.SourceFactory#create(Class, Properties)} with the deserialized properties.
 */
public class SourceConverter implements Converter {

    @Inject
    private EventBus eventBus;
    @Inject
    private Project project;
    @Inject
    private Source.SourceFactory sourceFactory;

    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        context.convertAnother(((Source) obj).getProperties());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            final Class<Source> sourceClass = (Class<Source>) project.xstream.getMapper().realClass(reader.getNodeName());
            final Properties properties = (Properties) context.convertAnother(null, Properties.class);

            // Although sources may block briefly upon creation, we intentionally do this in one thread.  This is to
            // ensure that other objects being deserialized (such as connections) don't try to access sources that
            // are in the process of loading.
            final Source source = sourceFactory.create(sourceClass, properties);

            // Instead of returning the source, post it to the event bus so both the core and GUI classes know it
            // exists.
            eventBus.post(new SourceAddedEvent(source));

            // Now that the source has been added it needs to be initialized
            // We do it safely here in case the source has changed in some way out
            // of our control. For example, if a webcam is no longer available.
            source.initializeSafely();

            return null;
        } catch (IOException | RuntimeException e) {
            throw new ConversionException("Error deserializing source", e);
        }
    }

    @Override
    public boolean canConvert(Class type) {
        return Source.class.isAssignableFrom(type);
    }
}
