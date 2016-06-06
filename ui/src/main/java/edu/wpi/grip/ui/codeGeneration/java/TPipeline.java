package edu.wpi.grip.ui.codegeneration.java;


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


public class TPipeline {

  protected List<TStep> steps;
  private int numSources;
  private int numOutputs;
  private Map<InputSocket, TOutput> connections;


  public TPipeline(Pipeline pipeline) {
    this.steps = new ArrayList<TStep>();
    this.numSources = 0;
    this.numOutputs = 0;
    connections = new HashMap<InputSocket, TOutput>();
    set(pipeline);
  }

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
        if(type.equals("Type")){
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

  protected TInput createInput(String type, String name, String value) {
    if(type.equals("Number")){
      if(value.contains(".")){
        type = "Double";
      }
      else{
        type = "Integer";
      }
    }
    if (value.contains("Optional.empty") || value.contains("Connection") || value.contains
        ("ContoursReport")) {
      int s = numSources;
      numSources++;
      value = "source" + s;
    } else if (value.contains("null")) {
        value = "null";
    }

    if (type.contains("CoreEnum")) {
      return new TInput("Integer", name, "Core." + value);
    } else if(type.contains("Enum")){
      return new TInput("Integer", name, "Imgproc." + value);
    }
    else if(type.equals("MaskSize") || type.contains("Type") || type.equals("Interpolation")){
      return new TInput(type, name, type + ".get(\"" + value+"\")");
    }
    else if (type.equals("String") ) {
      return new TInput(type, name, "\"" + value + "\"");
    } else if (type.equals("List")) {
      return new TInput("double[]", name, "{" + value.substring(1, value.length() - 1) + "}");
    } else {
      return new TInput(type, name, value);
    }
  }

  public List<TStep> getSteps() {
    return this.steps;
  }

  public static String updateOp(String opName) {
    return TemplateMethods.opName(opName);
  }

  public List<TStep> getUniqueSteps() {
    List<TStep> out = new ArrayList<TStep>();
    for (TStep step: steps) {
        out.removeIf(s -> s.name().equals(step.name()));
        out.add(step);
    }
    return out;
  }

  public int getNumSources(){
    return numSources;
  }

  public List<TSocket> getSources(){
    List<TSocket> sources = new ArrayList<TSocket>();
    for(TStep step: steps){
      for(TInput input: step.getInputs()){
        if(input.value().contains("source")){
          sources.add(input);
        }
      }
    }
    return sources;
  }

  public List<TStep> getMovingThresholds(){
    List<TStep> moving = new ArrayList<TStep>();
    for(TStep step: steps){
        if(step.name().equals("Threshold_Moving")){
          moving.add(step);
        }
    }
    return moving;
  }


}
