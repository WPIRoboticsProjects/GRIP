package edu.wpi.grip.ui.codegeneration;

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
    if (value.contains("Optional[")) {
      return value.substring(value.indexOf("[") + 1, value.lastIndexOf("]"));
    } else {
      return value;
    }
  }

  public static String parseSocketName(Socket socket) {
    String name = socket.getSocketHint().getIdentifier();
    return toCamelCase(name);
  }

  public static String toCamelCase(final String init) {
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
  }

  public static String parseSocketType(Socket socket) {
    String type = socket.getSocketHint().getType().getSimpleName();
    if(socket.getValue().equals(SocketHint.View.SELECT)) {
      if (!(type.equals("Type") || type.equals("MaskSize") || type.equals("FlipCode"))) {
        type = "Enum" + type;
      }
    }


    if (type.equals("Number")) {
      return "Double";
    } else {
      return type;
    }
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


}
