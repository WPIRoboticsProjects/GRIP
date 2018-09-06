package edu.wpi.grip.core.operations;


import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Converts a string of Python Code or a Python File into something the {@link
 * PythonScriptOperation} can handle.
 */
public abstract class PythonScriptFile {

  static {
    Properties pythonProperties = new Properties();
    pythonProperties.setProperty("python.import.site", "false");
    PySystemState.initialize(pythonProperties, null);
  }

  /**
   * @param url The URL to get the script file from.
   * @return The constructed PythonScript file.
   * @throws IOException If the URL fails to open.
   */
  public static PythonScriptFile create(URL url) throws IOException {
    final String path = url.getPath();
    final String alternativeName = path.substring(1 + Math.max(path.lastIndexOf('/'), path
        .lastIndexOf('\\')));
    final PythonInterpreter interpreter = new PythonInterpreter();
    interpreter.execfile(url.openStream());
    return create(interpreter, alternativeName);
  }

  /**
   * @param code The code to create the file from.
   * @return The constructed PythonScript file.
   */
  public static PythonScriptFile create(String code) {
    final PythonInterpreter interpreter = new PythonInterpreter();
    interpreter.exec(code);
    return create(interpreter, null);
  }

  @SuppressWarnings("unchecked")
  private static PythonScriptFile create(PythonInterpreter interpreter, String alternativeName) {
    final PyString name = interpreter.get("name", PyString.class);
    final PyString summary = interpreter.get("summary", PyString.class);
    final List<SocketHint<PyObject>> inputSocketHints = interpreter.get("inputs", List.class);
    final List<SocketHint<PyObject>> outputSocketHints = interpreter.get("outputs", List.class);
    final PyFunction performFunction = interpreter.get("perform", PyFunction.class);
    return new PythonScriptFileImpl(
        name == null ? alternativeName : name.toString(),
        summary == null ? "" : summary.toString(),
        inputSocketHints,
        outputSocketHints,
        performFunction);
  }

  public abstract String name();

  public abstract String summary();

  public abstract List<SocketHint<PyObject>> inputSocketHints();

  public abstract List<SocketHint<PyObject>> outputSocketHints();

  public abstract PyFunction performFunction();

  /**
   * Converts this file into a {@link PythonScriptOperation}.
   *
   * @param isf Input Socket Factory
   * @param osf Output Socket Factory
   * @return The meta data for a {@link PythonScriptOperation}
   */
  public final OperationMetaData toOperationMetaData(InputSocket.Factory isf, OutputSocket
      .Factory osf) {
    return new OperationMetaData(PythonScriptOperation.descriptionFor(this), () -> new
        PythonScriptOperation(isf, osf, this));
  }

  private static final class PythonScriptFileImpl extends PythonScriptFile {

    private final String name;
    private final String summary;
    private final List<SocketHint<PyObject>> inputSocketHints;
    private final List<SocketHint<PyObject>> outputSocketHints;
    private final PyFunction performFunction;

    public PythonScriptFileImpl(String name,
                                String summary,
                                List<SocketHint<PyObject>> inputSocketHints,
                                List<SocketHint<PyObject>> outputSocketHints,
                                PyFunction performFunction) {
      super();
      this.name = name;
      this.summary = summary;
      this.inputSocketHints = inputSocketHints;
      this.outputSocketHints = outputSocketHints;
      this.performFunction = performFunction;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String summary() {
      return summary;
    }

    @Override
    public List<SocketHint<PyObject>> inputSocketHints() {
      return inputSocketHints;
    }

    @Override
    public List<SocketHint<PyObject>> outputSocketHints() {
      return outputSocketHints;
    }

    @Override
    public PyFunction performFunction() {
      return performFunction;
    }
  }
}
