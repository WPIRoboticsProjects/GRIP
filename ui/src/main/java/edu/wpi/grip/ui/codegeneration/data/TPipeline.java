package edu.wpi.grip.ui.codegeneration.data;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.codegeneration.TemplateMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * TPipeline(template pipeline) is a data structure that holds the information about a pipeline
 * needed by the velocity templates to generate code.
 */
public class TPipeline {

  protected List<TStep> steps;
  private int numSources;
  private Map<InputSocket, TOutput> connections;
  private Map<String, Integer> uniqueSources;


  /**
   * Creates a Tpipeline from a pipeline
   *
   * @param pipeline The current grip pipeline.
   */
  public TPipeline(Pipeline pipeline) {
    this.uniqueSources = new HashMap<>();
    this.steps = new ArrayList<TStep>();
    this.numSources = 0;
    connections = new HashMap<InputSocket, TOutput>();
    set(pipeline);
  }

  /**
   * sets up the entire Tpipeline by creating the Tsteps
   *
   * @param pipeline The grip pipeline used to create the TPipeline.
   */
  public void set(Pipeline pipeline) {
    for (Step step : pipeline.getSteps()) {
      TStep tStep = makeStep(step.getOperationDescription().name().replaceAll(" ", "_"));
      steps.add(tStep);
      int numOutputs = 0;
      for (OutputSocket output : step.getOutputSockets()) {
        TOutput tOutput = new TOutput(TemplateMethods.parseSocketType(output), tStep.name()
            + tStep.num() + "Output" + numOutputs);
        numOutputs++;
        tStep.addOutput(tOutput);
        if (!output.getConnections().isEmpty()) {
          for (Object con : output.getConnections()) {
            connections.put(((Connection) con).getInputSocket(), tOutput);
          }
        }
      }
    }
    for (int i = 0; i < pipeline.getSteps().size(); i++) {
      TStep tStep = this.steps.get(i);
      for (InputSocket input : pipeline.getSteps().get(i).getInputSockets()) {
        TInput tInput;
        String type = TemplateMethods.parseSocketType(input);
        if (type.equals("Type")) {
          type = steps.get(i).name() + "Type";
        }
        type = type.replace("Number", "Double");
        String name = tStep.name() + tStep.num() + TemplateMethods.parseSocketName(input);
        if (!input.getConnections().isEmpty()) {
          if (connections.containsKey(input)) {
            tInput = new TInput(type, name, connections.get(input));
          } else {
            tInput = null;
            for (Object con : input.getConnections()) { 
              //Connections is a set. Should only have one element
              tInput = createInput(type, name, "Connection" + ((Connection) con).getOutputSocket()
                  .toString());
              break;  //If somehow there are multiple elements only care about first one.
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
  protected TInput createInput(String type, String name, String value) {
    if (value.contains("source") || value.contains("Connection")) {
      if (uniqueSources.containsKey(value) && value.contains("Connection")) {
        value = "source" + uniqueSources.get(value);
      } else {
        int s = numSources;
        numSources++;
        uniqueSources.put(value, s);
        value = "source" + s;
      }
    } else if (value.contains("null")) {
      value = "null";
    }
    return new TInput(type, name, value);
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
   * Creates a list of the unique operations in a pipeline.
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
   * returns the total number of sources.
   *
   * @return the number of sources
   */
  public int getNumSources() {
    return numSources;
  }

  /**
   * Returns a list of all of the sources in a the pipeline.
   *
   * @return the list of sources.
   */
  public List<TInput> getSources() {
    List<TInput> sources = new ArrayList<TInput>();

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

  private TStep makeStep(String opName){
    int count = 0;
    for(TStep step: steps){
      if(step.name().equals(opName)){
        count++;
      }
    }
    return new TStep(opName,count);
  }


}