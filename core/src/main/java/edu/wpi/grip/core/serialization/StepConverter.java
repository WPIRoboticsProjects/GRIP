package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.Optional;

import javax.inject.Inject;

/**
 * An XStream converter that converts a {@link Step} to and from a serialized representation. To
 * serialize a step, we just store an attribute with the name of the step.  To deserialize it, we
 * have to look up the operation with that name in the palette.
 */
public class StepConverter implements Converter {

  private static final String NAME_ATTRIBUTE = "name";
  private static final String ID_ATTRIBUTE = "id";

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
    writer.addAttribute(ID_ATTRIBUTE, step.getId());

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
    final Optional<OperationMetaData> operationMetaData = this.palette
        .getOperationByName(operationName);

    if (!operationMetaData.isPresent()) {
      throw new ConversionException("Unknown operation: " + operationName);
    }

    final String id = reader.getAttribute(ID_ATTRIBUTE);
    Step step = stepFactory.create(operationMetaData.get());
    if (id != null) {
      step.setId(id);
    }
    pipeline.addStep(step);

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
