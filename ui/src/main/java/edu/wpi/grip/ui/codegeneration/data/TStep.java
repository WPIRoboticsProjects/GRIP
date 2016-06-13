package edu.wpi.grip.ui.codegeneration.data;

import java.util.ArrayList;
import java.util.List;


public class TStep {
  private List<TInput> inputs;
  private List<TOutput> outputs;
  private String name;
  private int stepNum;

  public TStep(String name, int stepNum) {
    this.name = name.replaceAll(" ", "_");
    inputs = new ArrayList<TInput>();
    outputs = new ArrayList<TOutput>();
    this.stepNum = stepNum;
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

  public TInput getInput(int idx) {
    return inputs.get(idx);
  }

  public TOutput getOutput(int idx) {
    return outputs.get(idx);
  }

  public int num() {
    return stepNum;
  }

}
