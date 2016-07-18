package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.LineTypesEnum;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

public abstract class TemplateMethods {

  /**
   * gets a TemplateMethod of the desired language.
   *
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
        throw new IllegalArgumentException(lang
            + " is not a supported language for code generation.");
    }
  }

  /**
   * takes a socket and returns the value.
   *
   * @param socket the socket to be parsed
   * @return the value of the socket or "null" if there is no value
   */
  public static String parseSocketValue(Socket socket) {
    if (socket.getValue().isPresent() && !socket.getValue().get().toString()
        .contains("bytedeco") && !socket.getValue().get().toString().contains("Infinity")
        && !socket.getValue().get().toString().contains("ContoursReport")) {
      return socket.getValue().get().toString();
    }
    return "null";
  }

  /**
   * Takes a socket and returns the Name.
   *
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
    StringBuffer type = new StringBuffer();
    type.append(socket.getSocketHint().getType().getSimpleName());
    if (socket.getSocketHint().getView().equals(SocketHint.View.SELECT)
        && (BorderTypesEnum.class.equals(socket.getSocketHint().getType())
        || CmpTypesEnum.class.equals(socket.getSocketHint().getType())
        || LineTypesEnum.class.equals(socket.getSocketHint().getType()))) {
      type.append("CoreEnum");
    }

    return type.toString();
  }

  /**
   * Converts a name into the format for the correct language.
   *
   * @param name the unformatted name
   * @return the name after it has been formatted
   */
  public abstract String name(String name);

  /**
   * Converts a step into a string the represents the call of the operation in the correct language.
   * Used in the Templates
   *
   * @param step the step that will be called
   * @return a string that is the call to the operation.
   */
  public abstract String callOp(TStep step);

}
