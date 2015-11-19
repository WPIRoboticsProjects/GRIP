package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.*;

import java.io.Reader;
import java.io.Writer;

/**
 * Helper for saving and loading a processing pipeline to and from a file
 */
public class Project {

    private final XStream xstream = new XStream();
    private final Pipeline pipeline;

    public Project(EventBus eventBus, Pipeline pipeline, Palette palette) {
        this.pipeline = pipeline;

        this.xstream.registerConverter(new SocketConverter(xstream.getMapper(), pipeline));
        this.xstream.registerConverter(new StepConverter(eventBus, palette));
        this.xstream.registerConverter(new ConnectionConverter(eventBus));
        this.xstream.registerConverter(new InjectedObjectConverter<>(eventBus), XStream.PRIORITY_VERY_HIGH);
        this.xstream.registerConverter(new PythonScriptOperationConverter(), XStream.PRIORITY_VERY_HIGH);
        this.xstream.registerConverter(new MatConverter());

        this.xstream.processAnnotations(new Class[]{
                Pipeline.class, Step.class, Connection.class, InputSocket.class, OutputSocket.class});

        this.xstream.setMode(XStream.NO_REFERENCES);
    }

    /**
     * Load the project from the given reader
     */
    public void readFrom(Reader reader) {
        this.pipeline.clear();
        this.xstream.fromXML(reader);
    }

    /**
     * Save the project to the given writer.
     */
    public void writeTo(Writer writer) {
        this.xstream.toXML(this.pipeline, writer);
    }
}
