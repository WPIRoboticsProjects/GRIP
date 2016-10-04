package edu.wpi.grip.ui.codegeneration.data;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.TemplateMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.wpi.grip.core.OperationDescription.Category.NETWORK;

/**
 * TPipeline(template pipeline) is a data structure
 * that holds the information about a pipeline
 * needed by the velocity templates to generate code.
 */
public class TPipeline {

  protected List<TStep> steps;
  private int numSources;
  private final Map<InputSocket, TOutput> connections;
  private final Map<String, Integer> uniqueSources;
  // keep track of how many instances of each step are in the pipeline
  private final Map<String, Integer> stepInstances;


  /**
   * Creates a Tpipeline from a pipeline
   *
   * @param steps The list of steps from the pipeline to generate.
   */
  public TPipeline(List<Step> steps) {
    this.uniqueSources = new HashMap<>();
    this.steps = new ArrayList<>();
    this.numSources = 0;
    this.connections = new HashMap<>();
    this.stepInstances = new HashMap<>();
    // Only use non-publishing steps
    set(steps.stream()
        .filter(s -> !s.getOperationDescription().category().equals(NETWORK))
        .collect(Collectors.toList())
    );
  }

  /**
   * sets up the entire Tpipeline by creating the Tsteps
   *
   * @param pipeSteps The list of steps used to create the TPipeline.
   */
  private void set(List<Step> pipeSteps) {
    pipeSteps.stream()
        .map(s -> s.getOperationDescription().name())
        .forEach(n -> stepInstances.put(n, stepInstances.getOrDefault(n, 0) + 1));
    for (Step step : pipeSteps) {
      TStep tStep = makeStep(step.getOperationDescription().name().replaceAll(" ", "_"));
      steps.add(tStep);
      for (OutputSocket output : step.getOutputSockets()) {
        StringBuilder tNameBuilder = new StringBuilder(tStep.name());
        if (stepInstances.get(step.getOperationDescription().name()) > 1) {
          // Only add the number of there's more than one instance of that operation
          tNameBuilder
              .append('_')
              .append(tStep.num());
        }
        if (step.getOutputSockets().size() == 1) {
          // "Output" is descriptive enough if there's only one output
          tNameBuilder.append("_Output");
        } else {
          // Use specific names for each output
          tNameBuilder
              .append('_')
              .append(output.getSocketHint().getIdentifier().replace(' ', '_'));
        }
        TOutput tOutput = new TOutput(TemplateMethods.parseSocketType(output),
            tNameBuilder.toString());
        tStep.addOutput(tOutput);
        if (!output.getConnections().isEmpty()) {
          for (Object con : output.getConnections()) {
            Connection<?> c = (Connection<?>) con;
            if (!c.getInputSocket()
                .getStep()
                .map(s -> s.getOperationDescription().category())
                .get()
                .equals(NETWORK)) {
              // Only add the connection if it's not to a publishing step
              connections.put(c.getInputSocket(), tOutput);
            }
          }
        }
      }
    }
    for (int i = 0; i < pipeSteps.size(); i++) {
      TStep tStep = this.steps.get(i);
      for (InputSocket input : pipeSteps.get(i).getInputSockets()) {
        TInput tInput;
        String type = TemplateMethods.parseSocketType(input);
        if ("Type".equals(type)) {
          type = steps.get(i).name() + "Type";
        }
        type = type.replace("Number", "Double");
        StringBuilder nameBuilder = new StringBuilder(tStep.name());
        if (stepInstances.get(pipeSteps.get(i).getOperationDescription().name()) > 1) {
          nameBuilder
              .append('_')
              .append(tStep.num());
        }
        nameBuilder
            .append('_')
            .append(TemplateMethods.parseSocketName(input));
        String name = nameBuilder.toString();
        if (!input.getConnections().isEmpty()) {
          if (connections.containsKey(input)) {
            tInput = new TInput(type, name, connections.get(input));
          } else {
            tInput = null;
            for (Object con : input.getConnections()) {
              // Connections is a set. Should only have one element
              tInput = createInput(type, name,
                  "Connection" + ((Connection) con).getOutputSocket().toString());
            }
          }
        } else {
          tInput = createInput(type, name, TemplateMethods.parseSocketValue(input));
        }
        tStep.addInput(tInput);
      }
    }
  }

  /**
   * Creates a TInput for a TStep
   *
   * @param type  The data type of the input
   * @param name  The name of the input
   * @param value The value of the input
   * @return The generated TInput.
   */
  private TInput createInput(String type, String name, String value) {
    String outVal = value;
    if (value.contains("source") || value.contains("Connection")) {
      if (uniqueSources.containsKey(value) && value.contains("Connection")) {
        outVal = "source" + uniqueSources.get(value);
      } else {
        int s = numSources;
        numSources++;
        uniqueSources.put(value, s);
        outVal = "source" + s;
      }
    } else if (value.contains("null")) {
      outVal = "null";
    }
    return new TInput(type, name, outVal);
  }

  /**
   * used in Pipeline.vm to get all of the TSteps
   *
   * @return the TSteps that are in the TPipeline
   */
  public List<TStep> getSteps() {
    return this.steps;
  }

  /**
   * Creates a list of the unique operations in a pipeline. Used in templates.
   *
   * @return A list of the unique steps.
   */
  public List<TStep> getUniqueSteps() {
    List<TStep> out = new ArrayList<TStep>();
    for (TStep step : steps) {
      out.removeIf(s -> s.name().equals(step.name()));
      out.add(step);
    }
    return out;
  }

  /**
   * Returns a list of all of the sources in a the pipeline.
   *
   * @return the list of sources.
   */
  public List<TInput> getSources() {
    List<TInput> sources = new ArrayList<>();

    for (TStep step : steps) {
      for (TInput input : step.getInputs()) {
        if (input.value().contains("source")) {
          boolean add = true;
          for (TInput source : sources) {
            if (source.value().equals(input.value())) {
              add = false;
            }
          }
          if (add) {
            sources.add(input);
          }

        }
      }
    }
    return sources;
  }

  /**
   * Creates a list of all of the Moving_Threshold operations. Used in Templates.
   *
   * @return the list of Moving_Threshold operations.
   */
  public List<TStep> getMovingThresholds() {
    List<TStep> moving = new ArrayList<TStep>();
    for (TStep step : steps) {
      if (step.name().equals("Threshold_Moving")) {
        moving.add(step);
      }
    }
    return moving;
  }

  /**
   * creates a new step from a name
   *
   * @param opName the name of the step
   * @return a new step with the the opName and correct number.
   */
  private TStep makeStep(String opName) {
    int count = 0;
    for (TStep step : steps) {
      if (step.name().equals(opName)) {
        count++;
      }
    }
    return new TStep(opName, count);
  }

  /**
   * Gets the number of instances of an operation in the pipeline.
   *
   * @param operationName the name of the operation
   * @return the number of instances of an operation in this pipeline
   */
  public int instancesOfOperation(String operationName) {
    return stepInstances.getOrDefault(operationName, 0);
  }


}
