package edu.wpi.grip.ui.codegeneration.java;

import java.util.ArrayList;
import java.util.List;


public class TStep {
  private List<TInput> inputs;
  private List<TOutput> outputs;
  private String name;

  public TStep(String name) {
    this.name = name.replaceAll(" ", "_");
    inputs = new ArrayList<TInput>();
    outputs = new ArrayList<TOutput>();
  }

  public void addInput(TInput input) {
    inputs.add(input);
  }

  public void addOutput(TOutput output) {
    outputs.add(output);
  }

  public String name() {
    return this.name;
  }

  public List<TInput> getInputs() {
    return inputs;
  }

  public List<TOutput> getOutputs() {
    return outputs;
  }
  
  public TInput getInput(int idx){
	  return inputs.get(idx);
  }
  
  public TOutput getOutput(int idx){
	  return outputs.get(idx);
  }
  
  public String callOp(String num) {
	StringBuilder out = new StringBuilder();
    for (TInput input : inputs) {
      out.append(input.name() +num + ", ");
    }
    if (!outputs.isEmpty()) {
      for (int i = 0; i < outputs.size() - 1; i++) {
        out.append(outputs.get(i).name() + ", ");
      }
      out.append(outputs.get(outputs.size() - 1).name());
    }
    out.append(")");
    return out.toString();
  }

}
