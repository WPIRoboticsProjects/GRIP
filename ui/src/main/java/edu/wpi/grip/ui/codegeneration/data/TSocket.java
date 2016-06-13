package edu.wpi.grip.ui.codegeneration.data;


public class TSocket {
  protected String type;
  protected String name;
  private static final String MutableOf = "MutableOf<";

  public TSocket(String type, String name) {
    this(type);
    this.name = name;
  }

  public TSocket(String type) {
    setType(type);
  }

  public void setType(String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }

  public String name() {
    return name;
  }

  public String baseType() {
    if (!mutable()) {
      return type;
    } else {
      return baseTypeHelper(type);
    }
  }

  String baseTypeHelper(String type) {
    return type;
  }

  public boolean mutable() {
    return (type.equals("Integer") || type.equals("Double") || type.equals("Boolean") || type.equals("Number"));
  }

  public boolean number() {
    return type.contains("Integer") || type.contains("Double") || type.contains("Number");
  }
}
