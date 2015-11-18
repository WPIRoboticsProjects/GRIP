package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.InputSocket;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.events.StepAddedEvent;

import java.util.Optional;

/**
 * An XStream converter that converts a {@link Step} to and from a serialized representation.
 * <p>
 * To serialize a step, we just store an attribute with the name of the step.  To deserialize it, we have to look up
 * the operation with that name in the palette.
 */
class StepConverter implements Converter {

    private final static String NAME_ATTRIBUTE = "name";

    private final EventBus eventBus;
    private final Palette palette;

    public StepConverter(EventBus eventBus, Palette palette) {
        this.eventBus = eventBus;
        this.palette = palette;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Step step = ((Step) source);

        writer.addAttribute(NAME_ATTRIBUTE, step.getOperation().getName());

        // Also save any inputs that aren't connected, since their values are manually specified by the user
        for (InputSocket<?> socket : step.getInputSockets()) {
            if (socket.getConnections().isEmpty()) {
                context.convertAnother(socket);
            }
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final String operationName = reader.getAttribute(NAME_ATTRIBUTE);
        final Optional<Operation> operation = this.palette.getOperationByName(operationName);

        if (!operation.isPresent()) {
            throw new ConversionException("Unknown operation: " + operationName);
        }

        // Instead of simply returning the step and having XStream insert it into the pipeline using reflection, send a
        // StepAddedEvent.  This allows other interested classes (such as PipelineView) to also know when steps are added.
        this.eventBus.post(new StepAddedEvent(new Step(this.eventBus, operation.get())));

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            context.convertAnother(this, InputSocket.class);
            reader.moveUp();
        }

        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return Step.class.equals(type);
    }
}
