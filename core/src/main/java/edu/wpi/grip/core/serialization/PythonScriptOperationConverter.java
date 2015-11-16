package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import edu.wpi.grip.core.operations.PythonScriptOperation;

import java.io.IOException;
import java.net.URL;

/**
 * An XStream converter specifically for {@link PythonScriptOperation}.  Naively serializing a
 * <code>PythonScriptOperation</code> with the default settings will cause XStream to save and load the entire internal
 * structure of Jython's <code>PythonInterpreter</code> class.  All we really need to do is either save a URL or a
 * string of source code, and when we load a <code>PythonScriptOperation</code>,
 */
class PythonScriptOperationConverter implements Converter {
    private final String URL_NODE_NAME = "sourceURL";
    private final String CODE_NODE_NAME = "sourceCode";

    /**
     * Marshal a <code>PythonScriptOperation</code> into either a URL or a string of Python code, whichever was used
     * to construct the Python object.
     */
    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        PythonScriptOperation operation = (PythonScriptOperation) source;

        if (operation.getSourceURL().isPresent()) {
            writer.startNode(URL_NODE_NAME);
            context.convertAnother(operation.getSourceURL().get());
            writer.endNode();
        } else {
            writer.startNode(CODE_NODE_NAME);
            context.convertAnother(operation.getSourceCode().get());
            writer.endNode();
        }
    }

    /**
     * Unmarshal a <code>PythonScriptOperation</code> from either a URL or a string of Python code.
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        PythonScriptOperation operation = null;

        reader.moveDown();
        if (reader.getNodeName().equals(URL_NODE_NAME)) {
            try {
                operation = new PythonScriptOperation(new URL(reader.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            operation = new PythonScriptOperation(reader.getValue());
        }
        reader.moveUp();

        return operation;
    }

    /**
     * @return <code>true</code> only for <code>PythonScriptOperation</code>
     */
    @Override
    public boolean canConvert(Class type) {
        return PythonScriptOperation.class.equals(type);
    }
}