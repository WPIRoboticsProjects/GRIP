package edu.wpi.grip.core.serialization;

import com.google.common.eventbus.EventBus;
import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.util.Optional;

/**
 * Helper for saving and loading a processing pipeline to and from a file
 */
@Singleton
public class Project {

    @Inject
    private EventBus eventBus;
    @Inject
    private Pipeline pipeline;
    @Inject
    private Palette palette;

    protected final XStream xstream = new XStream();
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

    public void save(Writer writer) throws IOException {
        this.xstream.toXML(this.pipeline, writer);

        //The following is a work-around to make sure the previews are all recorded as closed
        //because sometimes the "desaturate" step can feed back into others when both previews are
        //opened at the same time (it's a race condition that we can't pin down)
        File file = this.file.get();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", oldtext = "";
        while ((line = reader.readLine()) != null) {
            oldtext += line + "\r\n";
        }
        reader.close();
        String newtext = oldtext.replaceAll("previewed=\"true\"", "previewed=\"false\"");
        FileWriter writer2 = new FileWriter(file);
        writer2.write(newtext);
        writer2.close();
    }
}
