package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.ui.codegeneration.java.TInput;
import edu.wpi.grip.ui.codegeneration.java.TStep;

public class TemplateMethods {

  private Pipeline pipeline;
  private Map<Connection, String> connections;
  private int numOutputs;
  private int numSources;

  public TemplateMethods() {
    connections = new HashMap<Connection, String>();
    numOutputs = 0;
    numSources = 0;
  }

  public void setPipeline(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public static String parseSocketValue(Socket socket) {
    String value = socket.getValue().toString();
    if (socket.getSocketHint().getView().equals(SocketHint.View.NONE)) {
      return "null" + value;
    } else if (value.contains("Optional[")) {
      return value.substring(value.indexOf("[") + 1, value.lastIndexOf("]"));
    } else {
      return value;
    }
  }

  public static String parseSocketName(Socket socket) {
    String name = socket.getSocketHint().getIdentifier();
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s", ""));
  }

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

  public static String opName(String name) {
    if (name.contains("CV ")) {
      return name.substring(3);
    } else return name;
  }

  public static String javaName(String name) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s", ""));
  }

  public static String callJavaOp(TStep step) {
    String num = "S" + step.num();
    StringBuilder out = new StringBuilder();
    out.append(javaName(step.name()));
    out.append("(");
    for (TInput input : step.getInputs()) {
      out.append(input.name());
      out.append(num);
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this.lastImage");
      out.append(num);
      out.append(", ");
    }
    if (!step.getOutputs().isEmpty()) {
      for (int i = 0; i < step.getOutputs().size() - 1; i++) {
        out.append(step.getOutputs().get(i).name());
        out.append(", ");
      }
      out.append(step.getOutputs().get(step.getOutputs().size() - 1).name());
    }
    out.append(")");
    return out.toString();
  }

  public static String cName(String name) {
    // name is something like "CV_medianBlur" or "Find_Contours"
    if (name.startsWith("CV_")) {
      // OpenCV operation
      String op = name.replaceFirst("CV_", "");
      if (op.contains("_")) {
        return "CV" + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, op.toLowerCase());
      } else {
        return "CV" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, op);
      }
    } else {
      // GRIP operation
      return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toUpperCase());
    }
  }

  public static String callCOp(TStep step){
    String num = "S" + step.num();
    StringBuilder out = new StringBuilder();
    out.append(cName(step.name()));
    out.append("(");
    for (TInput input : step.getInputs()) {
      out.append(input.name());
      out.append(num);
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this.lastImage");
      out.append(num);
      out.append(", ");
    }
    if (!step.getOutputs().isEmpty()) {
      for (int i = 0; i < step.getOutputs().size() - 1; i++) {
        out.append(step.getOutputs().get(i).name());
        out.append(", ");
      }
      out.append(step.getOutputs().get(step.getOutputs().size() - 1).name());
    }
    out.append(")");
    return out.toString();
  }

}