package edu.wpi.grip.ui.codegeneration.data;


public class TOutput extends TSocket {
  private int number;

  public TOutput(String type, int number) {
    super(type);
    this.number = number;
    super.name = "output" + number;
  }

}
