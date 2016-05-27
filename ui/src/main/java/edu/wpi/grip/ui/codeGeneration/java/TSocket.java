package edu.wpi.grip.ui.codegeneration.java;

/**
 * Created by Toby on 5/26/16.
 */
public class TSocket {
  protected String type;
  protected String name;

  public TSocket(String type, String name){
    this.type = type;
    this.name = name;
  }

  public TSocket(String type){
    this.type = type;
  }

  public String type(){
    return type;
  }

  public String name(){
    return name;
  }


}
