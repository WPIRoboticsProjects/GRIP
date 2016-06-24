package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.generated.opencv_core.enumeration.BorderTypesEnum;
import edu.wpi.grip.generated.opencv_core.enumeration.CmpTypesEnum;
import edu.wpi.grip.ui.codegeneration.data.TStep;

public abstract class TemplateMethods {

  protected Map<Connection, String> connections;
  protected int numOutputs;

  protected TemplateMethods() {
    connections = new HashMap<Connection, String>();
    numOutputs = 0;

  }

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

  public static String parseSocketValue(Socket socket){
	  if(socket.getValue().isPresent()){
		  return socket.getValue().get().toString();
	  }
	  return "null";
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

  public abstract String name(String name);

  public abstract String callOp(TStep step);

}