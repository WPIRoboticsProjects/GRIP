package edu.wpi.grip.ui.codegeneration.java;

import com.google.common.base.CaseFormat;

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

  public String javaName() {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, this.name.replaceAll("\\s",""));
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

  public int num(){ return stepNum;}
  
  public String callOp() {
    String num = "S" + this.stepNum;
    StringBuilder out = new StringBuilder();
    out.append(this.javaName());
    out.append("(");
    for (TInput input : inputs) {
      out.append(input.name() +num + ", ");
    }
    if(this.name().equals("Threshold_Moving")){
      out.append("this.lastImage" + num + ", ");
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
