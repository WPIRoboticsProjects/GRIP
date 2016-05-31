package edu.wpi.grip.ui.codegeneration.java;

/**
 * Created by Toby on 5/26/16.
 */
public class TOutput extends TSocket {
  private int number;

  public TOutput(String type, int number){
    super(type);
    if(super.type.equals("Number")){
      super.setType("Integer");
    }
    this.number = number;
    super.name = "output" +number;
  }

}
