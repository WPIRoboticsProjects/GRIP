package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import edu.wpi.grip.core.*;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An XStream converter for serializing and deserializing sockets.  Socket elements include indexes to indicate where
 * in the pipeline they are.  Input sockets can include values if specified, and output sockets can include boolean
 * attributes indicating if they are published and/or previewed.
 * <p>
 * Deserializing a socket doesn't create the socket itself - this is done when the step is created.  Instead, this
 * converter is used to reference particular sockets when defining values, published/previewed flags, and connections.
 */
class SocketConverter implements Converter {

    final private static String STEP_ATTRIBUTE = "step";
    final private static String SOCKET_ATTRIBUTE = "socket";
    final private static String PUBLISHED_ATTRIBUTE = "published";
    final private static String PREVIEWED_ATTRIBUTE = "previewed";

    final private Mapper mapper;
    final private Pipeline pipeline;

    public SocketConverter(Mapper mapper, Pipeline pipeline) {
        this.mapper = mapper;
        this.pipeline = pipeline;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Socket<?> socket = (Socket<?>) source;

        try {
            writer.startNode(mapper.serializedClass(socket.getClass()));

            // Save the location of the socket in the pipeline. TODO: Also handle sockets from a source
            if (socket.getStep().isPresent()) {
                final Step step = socket.getStep().get();
                final Socket<?>[] sockets = socket.getDirection() == Socket.Direction.INPUT ?
                        step.getInputSockets() : step.getOutputSockets();

                writer.addAttribute(STEP_ATTRIBUTE, String.valueOf(pipeline.getSteps().indexOf(step)));
                writer.addAttribute(SOCKET_ATTRIBUTE, String.valueOf(Arrays.asList(sockets).indexOf(socket)));
            }

            // Save whether or not output sockets are published and previewed
            if (socket.getDirection() == Socket.Direction.OUTPUT) {
                writer.addAttribute(PUBLISHED_ATTRIBUTE, String.valueOf(((OutputSocket) socket).isPublished()));
                writer.addAttribute(PREVIEWED_ATTRIBUTE, String.valueOf(((OutputSocket) socket).isPreviewed()));
            }

            // Save the value of input sockets that could possibly have been set with the GUI
            if (socket.getDirection() == Socket.Direction.INPUT
                    && socket.getConnections().isEmpty()
                    && socket.getSocketHint().getView() != SocketHint.View.NONE) {
                writer.startNode("value");
                if (List.class.isAssignableFrom(socket.getSocketHint().getType())) {
                    // XStream doesn't have a built-in converter for lists other than ArrayList
                    context.convertAnother(new ArrayList<>((List) socket.getValue()));
                } else {
                    context.convertAnother(socket.getValue());
                }
                writer.endNode();
            }

            writer.endNode();
        } catch (ConversionException e) {
            throw new ConversionException("Error serializing socket: " + socket, e);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            reader.moveDown();

            final String nodeName = reader.getNodeName();
            final int stepIndex = Integer.valueOf(reader.getAttribute(STEP_ATTRIBUTE));
            final int socketIndex = Integer.valueOf(reader.getAttribute(SOCKET_ATTRIBUTE));

            Socket.Direction direction;
            if (nodeName.equals(mapper.serializedClass(InputSocket.class))) {
                direction = Socket.Direction.INPUT;
            } else if (nodeName.equals(mapper.serializedClass(OutputSocket.class))) {
                direction = Socket.Direction.OUTPUT;
            } else {
                throw new IllegalArgumentException("Unexpected socket node name: " + nodeName);
            }

            // Look up the socket using the saved indexes.  Serializing sockets this way makes it so different things
            // (such as connections) can reference sockets in the pipeline.
            final Step step = pipeline.getSteps().get(stepIndex);
            final Socket socket = direction == Socket.Direction.INPUT ?
                    step.getInputSockets()[socketIndex] : step.getOutputSockets()[socketIndex];

            if (socket.getDirection() == Socket.Direction.OUTPUT) {
                ((OutputSocket) socket).setPublished(Boolean.valueOf(reader.getAttribute(PUBLISHED_ATTRIBUTE)));
                ((OutputSocket) socket).setPreviewed(Boolean.valueOf(reader.getAttribute(PREVIEWED_ATTRIBUTE)));
            }

            if (reader.hasMoreChildren()) {
                reader.moveDown();
                if (reader.getNodeName().equals("value")) {
                    socket.setValue(context.convertAnother(socket, getDeserializedType(socket)));
                } else {
                    throw new IllegalArgumentException("Unexpected node in socket: " + reader.getNodeName());
                }
                reader.moveUp();
            }

            reader.moveUp();

            return socket;
        } catch (Exception e) {
            throw new ConversionException("Error deserializing socket", e);
        }
    }

    /**
     * @param socket
     * @return The type to convert a serialized socket value into when deserializing.  This can't always be the type in
     * the socket, since sockets very often hold interfaces, and any number of classes may implement that interface.
     */
    private Class<?> getDeserializedType(Socket socket) {
        final Class<?> socketType = socket.getSocketHint().getType();

        if (!socketType.isInterface() && !Modifier.isAbstract(socketType.getModifiers())) {
            return socketType;
        } else if (socketType.equals(List.class)) {
            return ArrayList.class;
        } else if (socketType.equals(Number.class)) {
            return Double.class;
        }

        throw new ConversionException("Not sure what concrete class to use for socket with type " + socketType);
    }

    @Override
    public boolean canConvert(Class type) {
        return Socket.class.isAssignableFrom(type);
    }
}
