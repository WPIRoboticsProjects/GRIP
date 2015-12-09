package edu.wpi.grip.core.serialization;

import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.Pipeline;

import javax.inject.Inject;
import java.io.*;
import java.util.Optional;

/**
 * Helper for saving and loading a processing pipeline to and from a file
 */
public class Project {

    @Inject private XStream xstream;
    @Inject private Pipeline pipeline;

    private Optional<File> file = Optional.empty();

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
