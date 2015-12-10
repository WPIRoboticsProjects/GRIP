package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceAddedEvent;

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
 * {@link Source#createFromProperties(EventBus, Properties)} with the deserialized properties.
 */
public class SourceConverter implements Converter {

    private final EventBus eventBus;
    private final Mapper mapper;

    public SourceConverter(EventBus eventBus, Mapper mapper) {
        this.eventBus = eventBus;
        this.mapper = mapper;
    }

    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        context.convertAnother(((Source) obj).getProperties());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            final Class<Source> sourceClass = (Class<Source>) mapper.realClass(reader.getNodeName());
            final Properties properties = (Properties) context.convertAnother(null, Properties.class);

            // Although sources may block briefly upon creation, we intentionally do this in one thread.  This is to
            // ensure that other objects being deserialized (such as connections) don't try to access sources that
            // are in the process of loading.
            final Source source = sourceClass.newInstance();
            source.createFromProperties(eventBus, properties);

            // Instead of returning the source, post it to the event bus so both the core and GUI classes know it
            // exists.
            eventBus.post(new SourceAddedEvent(source));
            return null;
        } catch (InstantiationException | IllegalAccessException | IOException e) {
            throw new ConversionException("Error deserializing source", e);
        }
    }

    @Override
    public boolean canConvert(Class type) {
        return Source.class.isAssignableFrom(type);
    }
}
