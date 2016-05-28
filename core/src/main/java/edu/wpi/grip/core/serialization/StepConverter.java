package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;

import javax.inject.Inject;
import java.util.Optional;

/**
 * An XStream converter that converts a {@link Step} to and from a serialized representation.
 * <p>
 * To serialize a step, we just store an attribute with the name of the step.  To deserialize it, we have to look up
 * the operation with that name in the palette.
 */
public class StepConverter implements Converter {

    private final static String NAME_ATTRIBUTE = "name";

    @Inject
    private EventBus eventBus;
    @Inject
    private Palette palette;
    @Inject
    private Pipeline pipeline;
    @Inject
    private Step.Factory stepFactory;

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        final Step step = ((Step) source);

        writer.addAttribute(NAME_ATTRIBUTE, step.getOperationDescription().name());

        // Also save any sockets in the step
        for (InputSocket<?> socket : step.getInputSockets()) {
            context.convertAnother(socket);
        }

        for (OutputSocket<?> socket : step.getOutputSockets()) {
            context.convertAnother(socket);
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        final String operationName = reader.getAttribute(NAME_ATTRIBUTE);
        final Optional<OperationMetaData> operationMetaData = this.palette.getOperationByName(operationName);

        if (!operationMetaData.isPresent()) {
            throw new ConversionException("Unknown operation: " + operationName);
        }

        // Instead of simply returning the step and having XStream insert it into the pipeline using reflection, send a
        // StepAddedEvent.  This allows other interested classes (such as PipelineView) to also know when steps are added.
        pipeline.addStep(stepFactory.create(operationMetaData.get()));

        while (reader.hasMoreChildren()) {
            context.convertAnother(this, Socket.class);
        }

        return null;
    }

    @Override
    public boolean canConvert(Class type) {
        return Step.class.equals(type);
    }
}
