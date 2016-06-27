package edu.wpi.grip.ui.codegeneration.data;


public class TInput extends TSocket {
  private TOutput connectedOutput;
  private String value;

  public TInput(String type, String name, TOutput output) {
    super(type, name);
    this.connectedOutput = output;
  }

  public TInput(String type, String name, String value) {
    super(type, name);
    this.value = value;
  }

  public void setConnectedOutput(TOutput output) {
    this.value = null;
    this.connectedOutput = output;
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
    if (type.equals("Integer")) {
      return "int";
    }
    if (type.equals("Boolean")) {
      return "boolean";
    }
    if (type.equals("Double")) {
      return "double";
    }
    return type;
  }

  public boolean hasValue() {
    return value != null;
  }

}
