package edu.wpi.grip.core.serialization;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.StepIndexer;
import edu.wpi.grip.core.settings.CodeGenerationSettings;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.sockets.InputSocket;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Inject;

public final class PipelineConverter implements Converter {

  private static final Logger log = Logger.getLogger(PipelineConverter.class.getName());

  private static final String SOURCES_NODE_NAME = "sources";
  private static final String STEPS_NODE_NAME = "steps";
  private static final String CONNECTIONS_NODE_NAME = "connections";
  private static final String SETTINGS_NODE_NAME = "settings";
  private static final String CODE_GENERATION_SETTINGS_NODE_NAME = "codeGenerationSettings";

  @Inject
  private Pipeline pipeline;

  private static final Comparator<Connection> sourceConnectionsFirst =
      PipelineConverter::sortSourcesFirst;

  private final Comparator<Connection> byStartingStepIndex =
      (c1, c2) -> sortByStepIndex(pipeline, c1, c2);

  private static final Comparator<Connection> byInputSocketIndex =
      Comparator.comparingInt(PipelineConverter::getInputSocketIndex);

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Pipeline pipeline = (Pipeline) source;

    writer.startNode(SOURCES_NODE_NAME);
    context.convertAnother(new ArrayList<>(pipeline.getSources()));
    writer.endNode();

    writer.startNode(STEPS_NODE_NAME);
    context.convertAnother(new ArrayList<>(pipeline.getSteps()));
    writer.endNode();

    writer.startNode(CONNECTIONS_NODE_NAME);
    List<Connection> connections = pipeline.getConnections()
        .stream()
        .sorted(sourceConnectionsFirst
            .thenComparing(byStartingStepIndex)
            .thenComparing(byInputSocketIndex))
        .collect(Collectors.toList());
    context.convertAnother(connections);
    writer.endNode();

    writer.startNode(SETTINGS_NODE_NAME);
    context.convertAnother(pipeline.getProjectSettings());
    writer.endNode();

    writer.startNode(CODE_GENERATION_SETTINGS_NODE_NAME);
    context.convertAnother(pipeline.getCodeGenerationSettings());
    writer.endNode();
  }

  @Override
  public Pipeline unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      String nodeName = reader.getNodeName();
      switch (nodeName) {
        case SOURCES_NODE_NAME:
          context.convertAnother(this, List.class);
          break;
        case STEPS_NODE_NAME:
          context.convertAnother(this, List.class);
          break;
        case CONNECTIONS_NODE_NAME:
          context.convertAnother(this, List.class);
          break;
        case SETTINGS_NODE_NAME:
          context.convertAnother(this, ProjectSettings.class);
          break;
        case CODE_GENERATION_SETTINGS_NODE_NAME:
          context.convertAnother(this, CodeGenerationSettings.class);
          break;
        default:
          log.warning("Skipping unknown node '" + nodeName + "'");
          break;
      }
      reader.moveUp();
    }

    return pipeline;
  }

  @Override
  public boolean canConvert(Class type) {
    return Pipeline.class.equals(type);
  }

  /**
   * Orders two connections such that a connection from a source is placed before a connection from
   * the output of a step.
   *
   * @param c1 the first connection
   * @param c2 the second connection
   *
   * @return -1 if c1 is from a source and c2 is not; +1 if the reverse; 0 otherwise
   */
  private static int sortSourcesFirst(Connection c1, Connection c2) {
    if (c1.getOutputSocket().getSource().isPresent()) {
      if (c2.getOutputSocket().getSource().isPresent()) {
        return 0;
      } else {
        return -1;
      }
    } else if (c2.getOutputSocket().getSource().isPresent()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Orders two connections such that a connection from an earlier step in the pipeline is placed
   * before a connection from a later step.
   *
   * @param indexer the step indexer
   * @param c1      the first connection
   * @param c2      the second connection
   *
   * @return -1 is c1 is from a step prior to c2; +1 if the reverse; 0 otherwise
   */
  private static int sortByStepIndex(StepIndexer indexer, Connection<?> c1, Connection<?> c2) {
    if (c1.getOutputSocket().getStep().isPresent()) {
      if (c2.getOutputSocket().getStep().isPresent()) {
        return indexer.indexOf(c1.getOutputSocket().getStep().get())
            - indexer.indexOf(c2.getOutputSocket().getStep().get());
      } else {
        // c1 is from a step, but c2 is not
        return 1;
      }
    } else if (c2.getOutputSocket().getStep().isPresent()) {
      return -1;
    } else {
      return 0;
    }
  }

  /**
   * Gets the index of the input socket of a connection in its owner.
   *
   * @param connection the connection
   *
   * @return the index of the input socket of the connection in its owner
   */
  private static int getInputSocketIndex(Connection<?> connection) {
    InputSocket<?> inputSocket = connection.getInputSocket();
    return inputSocket.getStep().get().getInputSockets().indexOf(inputSocket);
  }

}
