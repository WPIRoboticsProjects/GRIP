package edu.wpi.grip.ui.codegeneration.java;

/**
 * Created by Toby on 5/26/16.
 */
public class TSocket {
  protected String type;
  protected String name;
  private static final String MutableOf = "MutableOf<";
  public TSocket(String type, String name){
    this(type);
    this.name = name;
  }

  public TSocket(String type){
    setType(type);
  }
  public void setType(String type){
	  if(type.equals("Integer")||type.equals("Double")||type.equals("Boolean")||type.equals("Number")){
	    	this.type = MutableOf+type+">";
	    }
	  else{
		  this.type = type;
	  }
  }
  public String type(){
    return type;
  }

  public String name(){
    return name;
  }

  public String baseType(){
	 if(!mutable()){	
		 return type;
	 }
	 else{
		 String retVal = type.replace(MutableOf, "").replace(">", "");
		 return retVal;
	 }
  }
  
  public boolean mutable(){
	  return type.contains(MutableOf);
  }

  public boolean number(){
	  return type.contains("Integer")||type.contains("Double");
  }
}
