package edu.wpi.grip.ui.codegeneration.java;

import java.util.ArrayList;
import java.util.List;


public class TStep {
  private List<TInput> inputs;
  private List<TOutput> outputs;
  private String name;

  public TStep(String name){
    this.name = name;
    inputs = new ArrayList<TInput>();
    outputs = new ArrayList<TOutput>();
  }

  public void addInput(TInput input){
    inputs.add(input);
  }

  public void addOutput(TOutput output){
    outputs.add(output);
  }

  public String name(){
    return name;
  }

  public List<TInput> getInputs(){
    return inputs;
  }

  public List<TOutput> getOutputs(){
    return outputs;
  }

}
