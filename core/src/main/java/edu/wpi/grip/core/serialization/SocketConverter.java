package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * An XStream converter for serializing and deserializing sockets.  Socket elements include indexes to indicate where
 * in the pipeline they are.  Input sockets can include values if specified, and output sockets can include boolean
 * attributes indicating if they are previewed.
 * <p>
 * Deserializing a socket doesn't create the socket itself - this is done when the step is created.  Instead, this
 * converter is used to reference particular sockets when defining values, previewed flags, and connections.
 */
public class SocketConverter implements Converter {

    final private static String STEP_ATTRIBUTE = "step";
    final private static String SOURCE_ATTRIBUTE = "source";
    final private static String SOCKET_ATTRIBUTE = "socket";
    final private static String PREVIEWED_ATTRIBUTE = "previewed";

    @Inject
    private Pipeline pipeline;
    @Inject
    private Project project;

    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Socket<?> socket = (Socket<?>) obj;

        try {
            writer.startNode(project.xstream.getMapper().serializedClass(socket.getClass()));

            // Save the location of the socket in the pipeline.
            socket.getStep().ifPresent(step -> {
                final List<? extends Socket> sockets = socket.getDirection() == Socket.Direction.INPUT ?
                        step.getInputSockets() : step.getOutputSockets();
                writer.addAttribute(STEP_ATTRIBUTE, String.valueOf(pipeline.getSteps().indexOf(step)));
                writer.addAttribute(SOCKET_ATTRIBUTE, String.valueOf(sockets.indexOf(socket)));
            });

            socket.getSource().ifPresent(source -> {
                final List<? extends Socket> sockets = source.getOutputSockets();
                writer.addAttribute(SOURCE_ATTRIBUTE, String.valueOf(pipeline.getSources().indexOf(source)));
                writer.addAttribute(SOCKET_ATTRIBUTE, String.valueOf(sockets.indexOf(socket)));
            });

            // Save whether or not output sockets are previewed
            if (socket.getDirection() == Socket.Direction.OUTPUT) {
                writer.addAttribute(PREVIEWED_ATTRIBUTE, String.valueOf(((OutputSocket) socket).isPreviewed()));
            }

            // Save the value of input sockets that could possibly have been set with the GUI
            if (socket.getDirection() == Socket.Direction.INPUT
                    && socket.getConnections().isEmpty()
                    && socket.getSocketHint().getView() != SocketHint.View.NONE) {
                writer.startNode("value");
                if (List.class.isAssignableFrom(socket.getSocketHint().getType()) && socket.getValue().isPresent()) {
                    // XStream doesn't have a built-in converter for lists other than ArrayList
                    context.convertAnother(new ArrayList<>((List<?>) socket.getValue().get()));
                } else {
                    context.convertAnother(socket.getValue().get());
                }
                writer.endNode();
            }

            writer.endNode();
        } catch (ConversionException e) {
            throw new ConversionException("Error serializing socket: " + socket, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        try {
            reader.moveDown();

            final Class<?> nodeClass = project.xstream.getMapper().realClass(reader.getNodeName());

            Socket.Direction direction;
            if (InputSocket.class.isAssignableFrom(nodeClass)) {
                direction = Socket.Direction.INPUT;
            } else if (OutputSocket.class.isAssignableFrom(nodeClass)) {
                direction = Socket.Direction.OUTPUT;
            } else {
                throw new IllegalArgumentException("Unexpected socket type: " + nodeClass.getName());
            }

            // Look up the socket using the saved indexes.  Serializing sockets this way makes it so different things
            // (such as connections) can reference sockets in the pipeline.
            Socket socket;
            if (reader.getAttribute(STEP_ATTRIBUTE) != null) {
                final int stepIndex = Integer.parseInt(reader.getAttribute(STEP_ATTRIBUTE));
                final int socketIndex = Integer.parseInt(reader.getAttribute(SOCKET_ATTRIBUTE));

                final Step step = pipeline.getSteps().get(stepIndex);
                socket = direction == Socket.Direction.INPUT ?
                        step.getInputSockets().get(socketIndex) : step.getOutputSockets().get(socketIndex);
            } else if (reader.getAttribute(SOURCE_ATTRIBUTE) != null) {
                final int sourceIndex = Integer.parseInt(reader.getAttribute(SOURCE_ATTRIBUTE));
                final int socketIndex = Integer.parseInt(reader.getAttribute(SOCKET_ATTRIBUTE));

                final Source source = pipeline.getSources().get(sourceIndex);
                socket = source.getOutputSockets().get(socketIndex);
            } else {
                throw new ConversionException("Sockets must have either a step or source attribute");
            }

            if (socket.getDirection() == Socket.Direction.OUTPUT) {
                ((OutputSocket) socket).setPreviewed(Boolean.parseBoolean(reader.getAttribute(PREVIEWED_ATTRIBUTE)));
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
        } catch (RuntimeException e) {
            throw new ConversionException("Error deserializing socket", e);
        }
    }

    /**
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
