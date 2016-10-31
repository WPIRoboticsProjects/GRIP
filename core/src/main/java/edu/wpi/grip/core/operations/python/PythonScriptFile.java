package edu.wpi.grip.core.operations.python;


import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;

import com.google.auto.value.AutoValue;

import org.python.core.PyException;
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
@AutoValue
public abstract class PythonScriptFile {

  /**
   * Template for custom python operations. Includes imports for sockets, as well as OpenCV
   * core and image processing classes.
   *
   * <p>The sample operation is a simple arithmetic "add" that hopefully shows how the script
   * should be written.</p>
   */
  public static final String TEMPLATE =
      "import edu.wpi.grip.core.sockets.SocketHints.Inputs as inputs\n"
          + "import edu.wpi.grip.core.sockets.SocketHints.Outputs as outputs\n"
          + "import org.bytedeco.javacpp.opencv_core as opencv_core\n"
          + "import org.bytedeco.javacpp.opencv_imgproc as opencv_imgproc\n\n"
          + "name = \"Addition Sample\"\n"
          + "summary = \"The sample python operation to add two numbers\"\n\n"
          + "inputs = [\n"
          + "    inputs.createNumberSpinnerSocketHint(\"a\", 0.0),\n"
          + "    inputs.createNumberSpinnerSocketHint(\"b\", 0.0),\n"
          + "]\n"
          + "outputs = [\n"
          + "    outputs.createNumberSocketHint(\"sum\", 0.0),\n"
          + "]\n\n\n" // two blank lines
          + "def perform(a, b):\n"
          + "    return a + b\n";

  static {
    Properties pythonProperties = new Properties();
    pythonProperties.setProperty("python.import.site", "false");
    PySystemState.initialize(pythonProperties, null);
  }

  /**
   * @param url The URL to get the script file from.
   * @return The constructed PythonScriptFile.
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
   * @return The constructed PythonScriptFile.
   * @throws PyException if the code has syntax or runtime errors
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
    return new AutoValue_PythonScriptFile(
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
  public final OperationMetaData toOperationMetaData(InputSocket.Factory isf,
                                                     OutputSocket.Factory osf) {
    return new OperationMetaData(
        PythonScriptOperation.descriptionFor(this),
        () -> new PythonScriptOperation(isf, osf, this)
    );
  }
}
