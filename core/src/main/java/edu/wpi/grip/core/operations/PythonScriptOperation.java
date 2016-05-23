package edu.wpi.grip.core.operations;

import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.Icon;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySequence;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * A class that implements an operation using Jython.  This enables users to write plugins for the application as
 * Python scripts.  Python script plugins should have global lists of SocketHints called "inputs" and "outputs" that
 * declare what parameters the script accepts and what outputs in produces. For example,
 * <p>
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
 * <p>
 * The script should also define a function "perform", which takes the same number of parameters as there are inputs
 * and returns the values for the outputs.  It can return a single value if there's one output, or a sequence type for
 * any number of values.
 * <p>
 * <pre>{@code
 * def perform(a, b):
 * return a + b
 * }</pre>
 * <p>
 * Lastly, the script can optionally have global "name" and "summary" strings to provide the user with more
 * information about what the operation does.
 */
public class PythonScriptOperation implements Operation {
    private static final String DEFAULT_NAME = "Python Operation";
    private static final Logger logger = Logger.getLogger(PythonScriptOperation.class.getName());


    private final PythonScriptFile scriptFile;
    private List<InputSocket> inputSockets; // intentionally using raw types
    private List<OutputSocket> outputSockets; // intentionally using raw types

    public static OperationDescription descriptionFor(PythonScriptFile pythonScriptFile) {
        return OperationDescription.builder()
                .name(pythonScriptFile.name())
                .summary(pythonScriptFile.summary())
                .icon(Icon.iconStream("python"))
                .category(OperationDescription.Category.MISCELLANEOUS)
                .build();
    }


    public PythonScriptOperation(InputSocket.Factory isf, OutputSocket.Factory osf, PythonScriptFile scriptFile) {
        checkNotNull(isf);
        checkNotNull(osf);

        this.scriptFile = checkNotNull(scriptFile);

        this.inputSockets = scriptFile.inputSocketHints().stream()
                .map(isf::create)
                .collect(Collectors.toList());

        this.outputSockets = scriptFile.outputSocketHints().stream()
                .map(osf::create)
                .collect(Collectors.toList());
    }


    /**
     * @return An array of Sockets, based on the global "inputs" list in the Python script
     */
    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.copyOf(inputSockets);
    }

    /**
     * @return An array of Sockets, based on the global "outputs" list in the Python script
     */
    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.copyOf(outputSockets);
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
     */
    @Override
    public void perform() {
        PyObject[] pyInputs = new PyObject[inputSockets.size()];
        for (int i = 0; i < inputSockets.size(); i++) {
            pyInputs[i] = Py.java2py(inputSockets.get(i).getValue().get());
        }

        try {
            PyObject pyOutput = this.scriptFile.performFunction().__call__(pyInputs);

            if (pyOutput.isSequenceType()) {
                /* If the Python function returned a sequence type, there must be multiple outputs for this step.
                 * Each element in the sequence is assigned to one output socket. */
                PySequence pySequence = (PySequence) pyOutput;
                Object[] javaOutputs = Py.tojava(pySequence, Object[].class);

                if (outputSockets.size() != javaOutputs.length) {
                    throw new IllegalArgumentException(wrongNumberOfArgumentsMsg(outputSockets.size(), javaOutputs.length));
                }

                for (int i = 0; i < javaOutputs.length; i++) {
                    outputSockets.get(i).setValue(javaOutputs[i]);
                }
            } else {
                /* If the Python script did not return a sequence, there should only be one output socket. */
                if (outputSockets.size() != 1) {
                    throw new IllegalArgumentException(wrongNumberOfArgumentsMsg(outputSockets.size(), 1));
                }

                Object javaOutput = Py.tojava(pyOutput, outputSockets.get(0).getSocketHint().getType());
                outputSockets.get(0).setValue(javaOutput);
            }
        } catch (RuntimeException e) {
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
