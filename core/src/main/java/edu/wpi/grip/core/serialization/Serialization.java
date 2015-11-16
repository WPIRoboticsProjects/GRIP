package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.Pipeline;

import java.io.Reader;
import java.io.Writer;

/**
 * This class contains two methods for saving and loading a {@link Pipeline} object to and from a project file,
 * allowing the user to save his or her work on an algorithm and load it back up later.
 */
public class Serialization {
    private XStream xstream = new XStream();

    public Serialization(EventBus eventBus) {
        // The event bus is not serialized.  Instead, every instance is injected with the "global" application event
        // bus.
        this.xstream.registerConverter(new InjectedObjectConverter<EventBus>(eventBus), XStream.PRIORITY_VERY_HIGH);

        // Python operations are serialized by the URL or source code that the Python script came from, rather than
        // the raw PythonScriptOperation class data.
        this.xstream.registerConverter(new PythonScriptOperationConverter(), XStream.PRIORITY_VERY_HIGH);

        // Mats, which are used to store images in native memory, are similarly not directly serialized.
        this.xstream.registerConverter(new MatConverter());
    }

    /**
     * @return A Pipeline loaded from the given XML file.
     */
    public Pipeline loadPipeline(Reader reader) {
        Pipeline pipeline = (Pipeline) this.xstream.fromXML(reader);
        pipeline.register();

        return pipeline;
    }

    /**
     * Save the pipeline as XML to the given writer.
     */
    public void savePipeline(Pipeline pipeline, Writer writer) {
        xstream.toXML(pipeline, writer);
    }
}