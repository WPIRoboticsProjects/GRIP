package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.wpi.grip.core.Connection;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;

/**
 * Created by Toby on 5/25/16.
 */
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
    if(socket.getSocketHint().getView().equals(SocketHint.View.NONE)){
      return "null" + value;
    }
    else if (value.contains("Optional[")) {
      return value.substring(value.indexOf("[") + 1, value.lastIndexOf("]"));
    } else {
      return value;
    }
  }

  public static String parseSocketName(Socket socket) {
    String name = socket.getSocketHint().getIdentifier();
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s",""));
    //return toCamelCase(name);
  }

  /*public static String toCamelCase(final String init) {
    if (init == null)
      return null;

    final StringBuilder ret = new StringBuilder(init.length());

    boolean firstWord = true;
    for (final String word : init.split(" ")) {
      if (firstWord) {
        firstWord = false;
        ret.append(word.substring(0).toLowerCase());
      } else if (!word.isEmpty()) {
        ret.append(word.substring(0, 1).toUpperCase());
        ret.append(word.substring(1).toLowerCase());
      }
    }

    return ret.toString();
  }*/

  public static String parseSocketType(Socket socket) {
    String type = socket.getSocketHint().getType().getSimpleName();
    if(socket.getSocketHint().getView().equals(SocketHint.View.SELECT)){
      if(type.contains("BorderTypes") || type.contains("CmpTypes")){
        type += "CoreEnum";
      }
    }
    else if(type.equals("ContoursReport")){
      type = "ArrayList<MatOfPoint>";
    }
    else if(type.equals("LinesReport")){
      type = "ArrayList<Line>";
    }
      return type;
  }

  public static String opName(String name) {
    if (name.contains("CV ")) {
      return name.substring(3);
    } else return name;
  }

  public String opArgs(Step step) {
    String args = "";
    for (InputSocket input : step.getInputSockets()) {
      args += parseSocketName(input) + ", ";
    }
    for (int i = 0; i < step.getOutputSockets().size() - 1; i++) {
      args += findConnectionName(step.getOutputSockets().get(i).getConnections()) + ", ";
    }
    args += findConnectionName(step.getOutputSockets().get(step.getOutputSockets().size() - 1).getConnections());
    return args;
  }

  public String findConnectionName(Set<Connection> connections) {
    for (Connection connection : connections) {
      if (this.connections.containsKey(connection)) {
        return this.connections.get(connection);
      }
    }
    return newSource();
  }

  public String storeOutput(Set<Connection> connections) {
    String connectionName = "output" + this.numOutputs;
    for (Connection connection : connections) {
      this.connections.put(connection, connectionName);
    }
    numOutputs++;
    return connectionName;
  }

  public String newSource() {
    String name = "sources.get(source" + numSources + ")";
    numSources++;
    return name;
  }

  public static String cName(String name){
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

}
