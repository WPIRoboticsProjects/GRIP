package edu.wpi.grip.ui.codegeneration.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.TemplateMethods;


/**
 * TPipeline(template pipeline) is a data structure that holds the information about a pipeline
 * needed by the velocity templates to generate code.
 */
public class TPipeline {

  protected List<TStep> steps;
  private int numSources;
  private int numOutputs;
  private Map<InputSocket, TOutput> connections;


  /**
   * Creates a Tpipeline from a pipeline
   *
   * @param pipeline The current grip pipeline.
   */
  public TPipeline(Pipeline pipeline) {
    this.steps = new ArrayList<TStep>();
    this.numSources = 0;
    this.numOutputs = 0;
    connections = new HashMap<InputSocket, TOutput>();
    set(pipeline);
  }

  /**
   * sets up the entire Tpipeline by creating the Tsteps
   *
   * @param pipeline The grip pipeline used to create the TPipeline.
   */
  public void set(Pipeline pipeline) {
    int count = 0;
    for (Step step : pipeline.getSteps()) {
      TStep tStep = new TStep(step.getOperationDescription().name(), count);
      steps.add(tStep);
      for (OutputSocket output : step.getOutputSockets()) {
        TOutput tOutput = new TOutput(TemplateMethods.parseSocketType(output), numOutputs);
        numOutputs++;
        tStep.addOutput(tOutput);
        if (!output.getConnections().isEmpty()) {
          for (Object con : output.getConnections()) {
            connections.put(((Connection) con).getInputSocket(), tOutput);
          }
        }
      }
      count++;
    }
    for (int i = 0; i < pipeline.getSteps().size(); i++) {
      for (InputSocket input : pipeline.getSteps().get(i).getInputSockets()) {
        TInput tInput;
        String type = TemplateMethods.parseSocketType(input);
        if (type.equals("Type")) {
          type = steps.get(i).name() + "Type";
        }
        type = type.replace("Number", "Double");
        String name = TemplateMethods.parseSocketName(input);
        if (!input.getConnections().isEmpty()) {
          if (connections.containsKey(input)) {
            tInput = new TInput(type, name, connections.get(input));
          } else {
            tInput = createInput(type, name, "Connection");
          }
        } else {
          tInput = createInput(type, name, TemplateMethods.parseSocketValue(input));
        }
        this.steps.get(i).addInput(tInput);
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
  protected TInput createInput(String type, String name, String value) {
    if (value.contains("Optional.empty") || value.contains("Connection") || value.contains
        ("ContoursReport")) {
      int s = numSources;
      numSources++;
      value = "source" + s;
    } else if (value.contains("null")) {
      value = "null";
    }
    return new TInput(type, name, value);
  }

  /**
   * used in Pipeline.vm to get all of the TSteps
   * @return the TSteps that are in the TPipeline
   */
  public List<TStep> getSteps() {
    return this.steps;
  }

  /**
   * Creates a list of the unique operations in a pipeline.
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
   * returns the total number of sources
   * @return the number of sources
   */
  public int getNumSources() {
    return numSources;
  }

  /**
   * Returns a list of all of the sources in a the pipeline.
   * @return the list of sources.
   */
  public List<TSocket> getSources() {
    List<TSocket> sources = new ArrayList<TSocket>();
    for (TStep step : steps) {
      for (TInput input : step.getInputs()) {
        if (input.value().contains("source")) {
          sources.add(input);
        }
      }
    }
    return sources;
  }

  /**
   * creates a list of all of the Moving_Threshold operations.
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


}