package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.events.ConnectionAddedEvent;

/**
 * An XStream converter that marshals and unmarshals {@link Connection}s.
 * <p>
 * A marshalled connection stores indexes indicating the output and input sockets it connects.
 */
class ConnectionConverter implements Converter {

    private final EventBus eventBus;

    public ConnectionConverter(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Connection<?> connection = (Connection<?>) source;
        context.convertAnother(connection.getOutputSocket());
        context.convertAnother(connection.getInputSocket());
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        OutputSocket<?> outputSocket = null;
        InputSocket<?> inputSocket = null;

        // Read the child nodes of this connection. An output socket and an input socket should be defined.
        while (reader.hasMoreChildren()) {
            Socket<?> tmp = (Socket<?>) context.convertAnother(null, Socket.class);
            if (tmp.getDirection() == Socket.Direction.INPUT) {
                inputSocket = (InputSocket<?>) tmp;
            } else {
                outputSocket = (OutputSocket<?>) tmp;
            }
        }

        if (inputSocket == null || outputSocket == null) {
            throw new ConversionException("Input and/or output was not specified in connection");
        }

        // Send the new connection as an event, so the GUI and other classes can listen for new connections even if regardless
        // of if they came from a serialized file or not.
        this.eventBus.post(new ConnectionAddedEvent(new Connection(this.eventBus, outputSocket, inputSocket)));
        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return Connection.class.equals(type);
    }
}
