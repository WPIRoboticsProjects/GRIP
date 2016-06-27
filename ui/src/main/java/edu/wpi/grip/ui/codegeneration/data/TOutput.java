package edu.wpi.grip.ui.codegeneration.data;


public class TOutput extends TSocket {
  private int number;

  /**
   * Constructor that creates a new template output socket.
   * @param type the type of the output.
   * @param number the step number of the output.
   */
  public TOutput(String type, int number) {
    super(type);
    this.number = number;
    super.name = "output" + number;
  }

}
