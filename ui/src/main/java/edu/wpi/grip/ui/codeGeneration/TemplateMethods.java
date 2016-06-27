package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;


public abstract class TemplateMethods {

  protected Map<Connection, String> connections;
  protected int numOutputs;

  protected TemplateMethods() {
    connections = new HashMap<Connection, String>();
    numOutputs = 0;

  }

  /**
   * gets a TemplateMethod of the desired language.
   * @param lang the desired language that will be exported
   * @return The template method of the correct language.
   */
  public static TemplateMethods get(Language lang) {
    switch (lang) {
      case JAVA:
        return new JavaTMethods();
      case PYTHON:
        return new PythonTMethods();
      case CPP:
        return new CppTMethods();
      default:
        throw new IllegalArgumentException(lang.toString()
            + " is not a supported language for code generation.");
    }
  }

  /**
   * takes a socket and returns the value.
   * @param socket the socket to be parsed
   * @return the value of the socket or "null" if there is no value
   */
  public static String parseSocketValue(Socket socket) {
    if (socket.getValue().isPresent() && !socket.getValue().get().toString().contains("bytedeco")) {
      return socket.getValue().get().toString();
    }
    return "null";
  }

  /**
   * Takes a socket and returns the Name.
   * @param socket the socket to be parsed
   * @return the name in Lower camel case.
   */
  public static String parseSocketName(Socket socket) {
    String name = socket.getSocketHint().getIdentifier();
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s", ""));
  }

  /**
   * Takes a socket and returns the type as a string.
   *
   * @param socket The socket that will be parsed.
   * @return The type of the socket with any needed additional information.
   */
  public static String parseSocketType(Socket socket) {
    String type = socket.getSocketHint().getType().getSimpleName();
    if (socket.getSocketHint().getView().equals(SocketHint.View.SELECT)) {
      if (BorderTypesEnum.class.equals(socket.getSocketHint().getType()) || CmpTypesEnum.class
          .equals(socket.getSocketHint().getType())) {
        type += "CoreEnum";
      }
    }
    return type;
  }

  public abstract String name(String name);

  public abstract String callOp(TStep step);

}