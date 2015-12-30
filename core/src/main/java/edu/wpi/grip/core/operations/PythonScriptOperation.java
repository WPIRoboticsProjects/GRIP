package edu.wpi.grip.core.operations;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.*;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;


/**
 * A class that implements an operation using Jython.  This enables users to write plugins for the application as
 * Python scripts.  Python script plugins should have global lists of SocketHints called "inputs" and "outputs" that
 * declare what parameters the script accepts and what outputs in produces. For example,
 *
 * <pre>{@code
 *    import edu.wpi.grip.core as grip
 *    import java.lang.Integer
 *
 *    inputs = [
 *        grip.SocketHint("a", java.lang.Integer, grip.SocketHint.View.SLIDER, (0, 100), 75),
 *        grip.SocketHint("b", java.lang.Integer, grip.SocketHint.View.SLIDER, (0, 100), 25),
 *    ]
 *
 *    outputs = [
 *        grip.SocketHint("c", java.lang.Integer),
 *    ]
 * }</pre>
 *
 * The script should also define a function "perform", which takes the same number of parameters as there are inputs
 * and returns the values for the outputs.  It can return a single value if there's one output, or a sequence type for
 * any number of values.
 *
 * <pre>{@code
 * def perform(a, b):
 * return a + b
 * }</pre>
 *
 * Lastly, the script can optionally have global "name" and "description" strings to provide the user with more
 * information about what the operation does.
 */
public class PythonScriptOperation implements Operation {

    static {
        Properties pythonProperties = new Properties();
        pythonProperties.setProperty("python.import.site", "false");
        PySystemState.initialize(pythonProperties, null);
    }

    private static final String DEFAULT_NAME = "Python Operation";
    private static final String DEFAULT_DESCRIPTION = "";
    private static final Logger logger =  Logger.getLogger(PythonScriptOperation.class.getName());


    // Either a URL or a String of literal source code is stored in this field.  This allows a PythonScriptOperation to
    // be serialized as a reference to some code rather than trying to save a bunch of Jython internal structures to a
    // file, which is what would automatically happen otherwise.
    private final Optional<URL> sourceURL;
    private final Optional<String> sourceCode;

    private final PythonInterpreter interpreter = new PythonInterpreter();

    private List<SocketHint<PyObject>> inputSocketHints;
    private List<SocketHint<PyObject>> outputSocketHints;
    private PyFunction performFunction;
    private PyString name;
    private PyString description;

    public PythonScriptOperation(URL url) throws PyException, IOException {
        this.sourceURL = Optional.of(url);
        this.sourceCode = Optional.empty();
        this.interpreter.execfile(url.openStream());
        this.getPythonVariables();

        if (this.name == null) {
            // If a name of the operation wasn't specified in the script, use the basename of the URL
            final String path = url.getPath();
            this.name = new PyString(path.substring(1 + Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"))));
        }

        if (this.description == null) {
            this.description = new PyString(DEFAULT_DESCRIPTION);
        }
    }

    public PythonScriptOperation(String code) throws PyException {
        this.sourceURL = Optional.empty();
        this.sourceCode = Optional.of(code);
        this.interpreter.exec(code);
        this.getPythonVariables();

        if (this.name == null) {
            this.name = new PyString(DEFAULT_NAME);
        }

        if (this.description == null) {
            this.description = new PyString(DEFAULT_DESCRIPTION);
        }
    }

    private void getPythonVariables() throws PyException {
        this.inputSocketHints = this.interpreter.get("inputs", List.class);
        this.outputSocketHints = this.interpreter.get("outputs", List.class);
        this.performFunction = this.interpreter.get("perform", PyFunction.class);
        this.name = this.interpreter.get("name", PyString.class);
        this.description = this.interpreter.get("description", PyString.class);
    }

    public Optional<URL> getSourceURL() {
        return this.sourceURL;
    }

    public Optional<String> getSourceCode() {
        return this.sourceCode;
    }

    @Override
    public String getName() {
        return this.name.getString();
    }

    @Override
    public String getDescription() {
        return this.description.getString();
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/python.png"));
    }

    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of Sockets, based on the global "inputs" list in the Python script
     */
    @Override
    public InputSocket[] createInputSockets(EventBus eventBus) {
        InputSocket[] sockets = new InputSocket[this.inputSocketHints.size()];

        for (int i = 0; i < sockets.length; i++) {
            sockets[i] = new InputSocket<>(eventBus, this.inputSocketHints.get(i));
        }

        return sockets;
    }

    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of Sockets, based on the global "outputs" list in the Python script
     */
    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        OutputSocket[] sockets = new OutputSocket[this.outputSocketHints.size()];

        for (int i = 0; i < sockets.length; i++) {
            sockets[i] = new OutputSocket<>(eventBus, this.outputSocketHints.get(i));
        }

        return sockets;
    }

    /**
     * Perform the operation by calling a function in the Python script.
     * <p>
     * This method adapts each of the inputs into Python objects, calls the Python function, and then converts the
     * outputs of the function back into Java objects and assigns them to the outputs array.
     * <p>
     * The Python function should return a tuple, list, or other sequence containing the outputs.  If there is only
     * one output, it can just return a value.  Either way, the number of inputs and outputs should match up with the
     * number of parameters and return values of the function.
     *
     * @param inputs  An array obtained from {@link #createInputSockets(EventBus)}. The caller can set the value of
     *                each socket to an actual parameter for the operation.
     * @param outputs An array obtained from {@link #createOutputSockets(EventBus)}. The outputs of the operation will
     */
    @Override
    public void perform(InputSocket[] inputs, OutputSocket[] outputs) {
        PyObject[] pyInputs = new PyObject[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            Class<?> a = inputs[i].getSocketHint().getType();
            Class<?> b = inputs[i].getValue().getClass();

            pyInputs[i] = Py.java2py(inputs[i].getValue().get());
        }

        try {
            PyObject pyOutput = this.performFunction.__call__(pyInputs);

            if (pyOutput.isSequenceType()) {
                /* If the Python function returned a sequence type, there must be multiple outputs for this step.
                 * Each element in the sequence is assigned to one output socket. */
                PySequence pySequence = (PySequence) pyOutput;
                Object[] javaOutputs = Py.tojava(pySequence, Object[].class);

                if (outputs.length != javaOutputs.length) {
                    throw new RuntimeException(wrongNumberOfArgumentsMsg(outputs.length, javaOutputs.length));
                }

                for (int i = 0; i < javaOutputs.length; i++) {
                    outputs[i].setValue(javaOutputs[i]);
                }
            } else {
                /* If the Python script did not return a sequence, there should only be one output socket. */
                if (outputs.length != 1) {
                    throw new RuntimeException(wrongNumberOfArgumentsMsg(outputs.length, 1));
                }

                Object javaOutput = Py.tojava(pyOutput, outputs[0].getSocketHint().getType());
                outputs[0].setValue(javaOutput);
            }
        } catch (Exception e) {
            /* Exceptions can happen if there's a mistake in a Python script, so just print a stack trace and leave the
             * current state of the output sockets alone.
             *
             * TODO: communicate the error to the GUI.
             */
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private static String wrongNumberOfArgumentsMsg(int expected, int actual) {
        return "Wrong number of outputs from Python script (expected " + expected + ", got " + actual + ")";
    }
}
