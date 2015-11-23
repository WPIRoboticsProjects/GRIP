package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;

import java.io.*;
import java.util.Optional;

/**
 * Helper for saving and loading a processing pipeline to and from a file
 */
public class Project {

    private final XStream xstream = new XStream();
    private final Pipeline pipeline;
    private Optional<File> file = Optional.empty();

    public Project(EventBus eventBus, Pipeline pipeline, Palette palette) {
        this.pipeline = pipeline;

        this.xstream.registerConverter(new StepConverter(eventBus, palette));
        this.xstream.registerConverter(new SourceConverter(eventBus, xstream.getMapper()));
        this.xstream.registerConverter(new SocketConverter(xstream.getMapper(), pipeline));
        this.xstream.registerConverter(new ConnectionConverter(eventBus));

        this.xstream.processAnnotations(new Class[]{
                Pipeline.class, Step.class, Connection.class, InputSocket.class, OutputSocket.class,
                ImageFileSource.class, CameraSource.class
        });

        this.xstream.setMode(XStream.NO_REFERENCES);
    }

    /**
     * @return The file that this project is located in, if it was loaded from/saved to a file
     */
    public Optional<File> getFile() {
        return file;
    }

    public void setFile(Optional<File> file) {
        this.file = file;
    }

    /**
     * Load the project from a file
     */
    public void open(File file) throws IOException {
        this.open(new FileReader(file));
        this.file = Optional.of(file);
    }

    public void open(Reader reader) {
        this.pipeline.clear();
        this.xstream.fromXML(reader);
    }

    /**
     * Save the project to a file
     */
    public void save(File file) throws IOException {
        this.save(new FileWriter(file));
        this.file = Optional.of(file);
    }

    public void save(Writer writer) {
        this.xstream.toXML(this.pipeline, writer);
    }
}
