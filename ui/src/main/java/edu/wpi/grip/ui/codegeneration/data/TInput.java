package edu.wpi.grip.ui.codegeneration.data;


public class TInput extends TSocket {
  private TOutput connectedOutput;
  private String value;

  /**
   * creates a new TInput with an output
   * @param type the type of the TInput.
   * @param name the GRIP name of the TInput.
   * @param output the output the the TInput is connected to.
   */
  public TInput(String type, String name, TOutput output) {
    super(type, name);
    this.connectedOutput = output;
  }

  /**
   * creates a new TInput with a preset value
   * @param type the type of the TInput.
   * @param name the GRIP name of the TInput.
   * @param value the preset value of the TInput represented as a string.
   */
  public TInput(String type, String name, String value) {
    super(type, name);
    this.value = value;
  }

  /**
   * Gets the value of the output.
   * @return returns the value of the output in the form of a string.
   */
  public String value() {
    if (value != null) {
      return value;
    } else {
      return connectedOutput.name();
    }
  }

  @Override
  String baseTypeHelper(String type) {
    if ("Integer".equals(type)) {
      return "int";
    }
    if ("Boolean".equals(type)) {
      return "boolean";
    }
    if ("Double".equals(type)) {
      return "double";
    }
    return type;
  }

  /**
   * Checks to see if the input has a value
   * @return true if there is a value. false if it is null.
   */
  public boolean hasValue() {
    return value != null;
  }

}
