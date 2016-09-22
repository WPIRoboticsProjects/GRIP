package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.operations.CVOperations;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.LineTypesEnum;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import org.apache.commons.lang3.text.WordUtils;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;

import java.nio.DoubleBuffer;
import java.util.Optional;

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
        throw new IllegalArgumentException(
            lang + " is not a supported language for code generation.");
    }
  }

  /**
   * takes a socket and returns the value.
   *
   * @param socket the socket to be parsed
   * @return the value of the socket or "null" if there is no value
   */
  public static String parseSocketValue(Socket socket) {
    if (socket.getValue().isPresent() && !socket.getValue().get().toString().contains("bytedeco")
        && !socket.getValue().get().toString().contains("Infinity")
        && !socket.getValue().get().toString().contains("ContoursReport")) {
      return socket.getValue().get().toString();
    } else {
      Optional initValOptional = socket.getSocketHint().createInitialValue();
      if (initValOptional.isPresent()) {
        Object initVal = initValOptional.get();
        String type = parseSocketType(socket);
        StringBuilder valueBuilder = new StringBuilder();
        if ("Point".equalsIgnoreCase(type)) {
          Point pointVal = (Point) initVal;
          valueBuilder.append('(').append(pointVal.x()).append(", ").append(pointVal.y())
              .append(')');
        } else if ("Size".equalsIgnoreCase(type)) {
          Size sizeVal = (Size) initVal;
          valueBuilder.append('(').append(sizeVal.width()).append(", ").append(sizeVal.height())
              .append(')');
        } else if ("Scalar".equals(type)) {
          Scalar scaleVal = (Scalar) initVal;
          DoubleBuffer buff = scaleVal.asBuffer();
          StringBuilder temp = new StringBuilder();
          temp.append('(').append(buff.get());
          while (buff.hasRemaining()) {
            temp.append(", ").append(buff.get());
          }
          temp.append(')');
          if (temp.toString().contains("E")) {
            valueBuilder.append("(-1)");
          } else {
            valueBuilder.append(temp.toString());
          }
        } else if ("Mat".equals(type)) {
          return "";
        }
        if (valueBuilder.length() > 0) {
          return valueBuilder.toString();
        }
      }
    }
    return "source";
  }

  /**
   * Takes a socket and returns the Name.
   *
   * @param socket the socket to be parsed
   * @return the name in Lower camel case.
   */
  public static String parseSocketName(Socket socket) {
    String name = socket.getSocketHint().getIdentifier();
    return WordUtils.capitalize(name).replaceAll("[\\s]+", "_");
  }

  /**
   * Takes a socket and returns the type as a string.
   *
   * @param socket The socket that will be parsed.
   * @return The type of the socket with any needed additional information.
   */
  public static String parseSocketType(Socket socket) {
    StringBuilder type = new StringBuilder();
    type.append(socket.getSocketHint().getType().getSimpleName());
    if (BorderTypesEnum.class.equals(socket.getSocketHint().getType())
        || CmpTypesEnum.class.equals(socket.getSocketHint().getType())
        || CVOperations.CVBorderTypesEnum.class.equals(socket.getSocketHint().getType())
        || LineTypesEnum.class.equals(socket.getSocketHint().getType())) {
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
   * Converts a name into the format for the correct language.
   *
   * @param name the unformatted name
   * @return the name after it has been formatted
   */
  public abstract String getterName(String name);

  /**
   * Converts a name into the format for the correct language.
   *
   * @param name the unformatted name
   * @return the name after it has been formatted
   */
  public abstract String setterName(String name);

  /**
   * Converts a step into a string the represents the call of the operation in the correct language.
   * Used in the Templates
   *
   * @param step the step that will be called
   * @return a string that is the call to the operation.
   */
  public abstract String callOp(TStep step);

}
