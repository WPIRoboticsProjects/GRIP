package edu.wpi.grip.core.serialization;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Helper for saving and loading a processing pipeline to and from a file
 */
@Singleton
public class Project {

    @Inject
    private Pipeline pipeline;

    protected final XStream xstream = new XStream(new PureJavaReflectionProvider());
    private Optional<File> file = Optional.empty();

    @Inject
    public void initialize(StepConverter stepConverter,
                           SourceConverter sourceConverter,
                           SocketConverter socketConverter,
                           ConnectionConverter connectionConverter,
                           ProjectSettingsConverter projectSettingsConverter) {
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(stepConverter);
        xstream.registerConverter(sourceConverter);
        xstream.registerConverter(socketConverter);
        xstream.registerConverter(connectionConverter);
        xstream.registerConverter(projectSettingsConverter);
        xstream.processAnnotations(new Class[]{Pipeline.class, Step.class, Connection.class, InputSocket.class,
                OutputSocket.class, ImageFileSource.class, MultiImageFileSource.class, CameraSource.class});
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
        try (final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            this.open(reader);
        }
        this.file = Optional.of(file);
    }

    @VisibleForTesting
    void open(Reader reader) {
        this.pipeline.clear();
        this.xstream.fromXML(reader);
    }

    /**
     * Save the project to a file
     */
    public void save(File file) throws IOException {
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            this.save(writer);
        }
        this.file = Optional.of(file);
    }

    public void save(Writer writer) {
        this.xstream.toXML(this.pipeline, writer);
    }
}
