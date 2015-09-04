package edu.wpi.grip.core.operations;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.Socket;
import edu.wpi.grip.core.SocketHint;
import org.python.core.*;
import org.python.util.PythonInterpreter;

import java.io.InputStream;
import java.util.List;

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
 * The script should also define a function "perform", which takes the same numper of parameters as there are inputs
 * and returns the values for the outputs.  It can return a single value if there's one output, or a sequence type for
 * any number of values.
 *
 * <pre>{@code
 * def perform(a, b):
 * return a + b
 * }</pre>
 */
public class PythonScriptOperation implements Operation {

    private final PythonInterpreter interpreter = new PythonInterpreter();

    private List<SocketHint<PyObject>> inputSocketHints;
    private List<SocketHint<PyObject>> outputSocketHints;
    private PyFunction performFunction;

    public PythonScriptOperation(InputStream code) throws PyException {
        this.interpreter.execfile(code);
        this.getPythonVariables();
    }

    public PythonScriptOperation(String code) throws PyException {
        this.interpreter.exec(code);
        this.getPythonVariables();
    }

    private void getPythonVariables() throws PyException {
        this.inputSocketHints = this.interpreter.get("inputs", List.class);
        this.outputSocketHints = this.interpreter.get("outputs", List.class);
        this.performFunction = this.interpreter.get("perform", PyFunction.class);
    }

    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of Sockets, based on the global "inputs" list in the Python script
     */
    @Override
    public Socket<?>[] createInputSockets(EventBus eventBus) {
        Socket[] sockets = new Socket[this.inputSocketHints.size()];

        for (int i = 0; i < sockets.length; i++) {
            sockets[i] = new Socket<>(eventBus, this.inputSocketHints.get(i));
        }

        return sockets;
    }

    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of Sockets, based on the global "outputs" list in the Python script
     */
    @Override
    public Socket<?>[] createOutputSockets(EventBus eventBus) {
        Socket[] sockets = new Socket[this.outputSocketHints.size()];

        for (int i = 0; i < sockets.length; i++) {
            sockets[i] = new Socket<>(eventBus, this.outputSocketHints.get(i));
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
    public void perform(Socket[] inputs, Socket[] outputs) {
        PyObject[] pyInputs = new PyObject[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            pyInputs[i] = Py.java2py(inputs[i].getValue());
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
             * TODO: This method should not throw (since it's an event handler), but it should somehow communicate the
             * error to the GUI. */
            e.printStackTrace();
        }
    }

    private static String wrongNumberOfArgumentsMsg(int expected, int actual) {
        return "Wrong number of outputs from Python script (expected " + expected + ", got " + actual + ")";
    }
}
