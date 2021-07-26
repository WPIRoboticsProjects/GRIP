package edu.wpi.grip.ui.codegeneration.data;

import java.util.ArrayList;
import java.util.List;


public class TStep {
  private final List<TInput> inputs;
  private final List<TOutput> outputs;
  private final String name;
  private final int stepNum;

  /**
   * This is a constructor for a Template step.
   * @param name The name of the step.
   * @param stepNum The number of the step in the pipeline.
   */
  public TStep(String name, int stepNum) {
    this.name = name;
    inputs = new ArrayList<TInput>();
    outputs = new ArrayList<TOutput>();
    this.stepNum = stepNum;
  }

  /**
   * Adds an already created TInput to a step.
   * @param input a TInput to be added
   */
  public void addInput(TInput input) {
    inputs.add(input);
  }

  /**
   * Adds an already created TOutput to a step.
   * @param output a TOutput to be added
   */
  public void addOutput(TOutput output) {
    outputs.add(output);
  }

  /**
   * gets the name of the step
   * @return the GRIP name of the step with underscores instead of spaces.
   */
  public String name() {
    return this.name;
  }

  /**
   * returns all of the inputs of a step.
   * @return all of the inputs of the step.
   */
  public List<TInput> getInputs() {
    return inputs;
  }

  /**
   * returns all of the Outputs of a step.
   * @return all of the Outputs of the step.
   */
  public List<TOutput> getOutputs() {
    return outputs;
  }

  /**
   * gets a specific Input for use in templates.
   * @param idx the index of the input.
   * @return the TOutput at the index.
   */
  public TInput getInput(int idx) {
    return inputs.get(idx);
  }

  /**
   * gets a specific Output for use in templates.
   * @param idx the index of the Output.
   * @return the TOutput at the index.
   */
  public TOutput getOutput(int idx) {
    return outputs.get(idx);
  }

  /**
   * The number of the step. Each different type of step is numbered separately.
   * @return the number of the step.
   */
  public int num() {
    return stepNum;
  }

}
