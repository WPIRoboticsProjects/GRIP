package edu.wpi.grip.core.operations


import edu.wpi.grip.core.OperationMetaData
import edu.wpi.grip.core.sockets.InputSocket
import edu.wpi.grip.core.sockets.OutputSocket
import edu.wpi.grip.core.sockets.SocketHint
import org.python.core.PyFunction
import org.python.core.PyString
import org.python.core.PySystemState
import org.python.util.PythonInterpreter
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Converts a string of Python Code or a Python File into something the [ ] can handle.
 */
data class PythonScriptFile private constructor(
        val name: String,
        val summary: String,
        val inputSocketHints: List<SocketHint<*>>,
        val outputSocketHints: List<SocketHint<*>>,
        val performFunction: PyFunction
) {


    /**
     * Converts this file into a [PythonScriptOperation].

     * @param isf Input Socket Factory
     * *
     * @param osf Output Socket Factory
     * *
     * @return The meta data for a [PythonScriptOperation]
     */
    fun toOperationMetaData(isf: InputSocket.Factory, osf: OutputSocket.Factory): OperationMetaData {
        return OperationMetaData(PythonScriptOperation.descriptionFor(this)) { PythonScriptOperation(isf, osf, this) }
    }

    private companion object {

        init {
            val pythonProperties = Properties()
            pythonProperties.setProperty("python.import.site", "false")
            PySystemState.initialize(pythonProperties, null)
        }

        /**
         * @param url The URL to get the script file from.
         * *
         * @return The constructed PythonScript file.
         * *
         * @throws IOException If the URL fails to open.
         */
        @JvmStatic
        @Throws(IOException::class)
        fun create(url: URL): PythonScriptFile {
            val path = url.path
            val alternativeName = path.subSequence(
                    1 + Math.max(path.lastIndexOf('/'),
                            path.lastIndexOf('\\')), path.length).toString()
            val interpreter = PythonInterpreter()
            interpreter.execfile(url.openStream())
            return create(interpreter, alternativeName)
        }

        /**
         * @param code The code to create the file from.
         * *
         * @return The constructed PythonScript file.
         */
        @JvmStatic
        fun create(code: String): PythonScriptFile {
            val interpreter = PythonInterpreter()
            interpreter.exec(code)
            return create(interpreter, null)
        }

        private fun convertPyToSocketHint(list: List<*>): List<SocketHint<*>> {
            return list.map {
                if (it is SocketHint<*>) {
                    it
                } else {
                    throw ClassCastException(
                            "Return from socket hint method must be type 'SocketHint' " +
                                    "but was $it")

                }
            }
        }

        private fun create(interpreter: PythonInterpreter, alternativeName: String?):
                PythonScriptFile {
            val name = interpreter.get<PyString>("name", PyString::class.java)
            val summary = interpreter.get<PyString>("summary", PyString::class.java)
            val inputSocketHints =
                    convertPyToSocketHint(interpreter.get<List<*>>("inputs", List::class.java))
            val outputSocketHints =
                    convertPyToSocketHint(interpreter.get<List<*>>("outputs", List::class.java))
            val performFunction = interpreter.get<PyFunction>("perform", PyFunction::class.java)
            return PythonScriptFile(
                    if (name == null) alternativeName!! else name.toString(),
                    if (summary == null) "" else summary.toString(),
                    inputSocketHints,
                    outputSocketHints,
                    performFunction)
        }
    }
}
