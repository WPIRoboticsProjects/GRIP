package edu.wpi.grip.ui.codegeneration.data;

/**
 * This class is a socket for a step and is extended by TOutput and TInput.
 */
public class TSocket {
  private String type;
  private final String name;

  /**
   * The default constructor for a Socket. Assigns member variable to values.
   * @param type the type of the socket. eg: (String, Mat).
   * @param name the GRIP name of the socket.
   */
  protected TSocket(String type, String name) {
    this.type = type;
    this.name = name;
  }

  /**
   * Sets the type of the socket to a value.
   * @param type The new type of the socket
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * gets the type of the socket.
   * @return the type of the socket.
   */
  public String type() {
    return type;
  }

  /**
   * the String that represents the socket.
   * @return the name of the socket.
   */
  public String name() {
    return name;
  }

  /**
   * This checks if a step is mutable and returns the type. Used in Templates.
   * @return The type of the socket.
   */
  public String baseType() {
    if (!mutable()) {
      return type;
    } else {
      return baseTypeHelper(type);
    }
  }

  /**
   * Can be overridden by a subclass. is overridden by TInput.
   * @param type the original type
   * @return the baseType
   */
  String baseTypeHelper(String type) {
    return type;
  }

  /**
   * Checks to see if the socket is mutable.
   * @return true if mutable. false if not mutable.
   */
  public boolean mutable() {
    return ("Integer".equals(type) || "Double".equals(type) || "Boolean".equals(type)
        || "Number".equals(type));
  }

  /**
   * checks to see if the socket is a number type.
   * @return true if it is a number.
   */
  public boolean number() {
    return "Integer".contains(type) || "Double".contains(type) || "Number".contains(type);
  }
}
