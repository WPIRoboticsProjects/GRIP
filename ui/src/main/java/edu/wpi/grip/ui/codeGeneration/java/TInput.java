package edu.wpi.grip.ui.codegeneration.java;


/**
 * Created by Toby on 5/26/16.
 */
public class TInput extends TSocket {
  private TOutput connectedOutput;
  private String value;

  public TInput(String type,String name,TOutput output){
    super(type, name);
    this.connectedOutput = output;
  }

  public TInput(String type,String name,String value){
    super(type, name);
    this.value = value;
  }

  public void setConnectedOutput(TOutput output){
    this.value = null;
    this.connectedOutput = output;
  }

  public String value(){
    if(value!=null){
      return value;
    }
    else{
      return connectedOutput.name();
    }
  }
  @Override
  String baseTypeHelper(String type){
	  if(type.equals("Integer")){
		  return "int";
	  }
	  if(type.equals("Boolean")){
		  return "boolean";
	  }
	  if(type.equals("Double")){
		  return "double";
	  }
	  return type;
  }
  
  public boolean hasValue(){
	  return value!=null;
  }

}
