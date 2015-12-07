package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.SocketHint;
import edu.wpi.grip.core.SocketHints;
import edu.wpi.grip.core.SwitchableSource;
import edu.wpi.grip.core.events.SourceStartedEvent;
import edu.wpi.grip.core.util.OpenCVUtility;
import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Source that supports multiple images. They can be toggled using {@link MultiImageFileSource#nextValue()} and
 * {@link MultiImageFileSource#previousValue()}
 */
public class MultiImageFileSource extends SwitchableSource {
    private static final String INDEX_PROPERTY = "index";
    private static final String SIZE_PROPERTY = "size";

    private final SocketHint<opencv_core.Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private OutputSocket<opencv_core.Mat> outputSocket;

    private EventBus eventBus;
    private List<String> paths;
    private int index = 0;

    /**
     * @param eventBus The event bus.
     * @param files A list of files to be loaded.
     * @param index The index to use as the first file that is in the socket.
     * @throws IOException If the source fails to load any of the images
     */
    public MultiImageFileSource(final EventBus eventBus, final List<File> files, int index) throws IOException {
        this.initialize(eventBus, files.stream()
                .map(file -> URLDecoder.decode(Paths.get(file.toURI()).toString()))
                .collect(Collectors.toList()), index);
    }

    public MultiImageFileSource(final EventBus eventBus, final List<File> files) throws IOException {
        this(eventBus, files, 0);
    }

    /**
     * Used only for serialization
     */
    public MultiImageFileSource(){ /* no op */ }


    @SuppressWarnings("unchecked")
    private void initialize(final EventBus eventBus, final List<String> paths, int index) throws IOException {
        this.eventBus = checkNotNull(eventBus, "Event bus can not be null");
        this.index = checkElementIndex(index, paths.size(), "File List Index");
        this.paths = paths;
        this.outputSocket = new OutputSocket(eventBus, imageOutputHint);
        for (String path : paths) {
            // Ensure that all of the images can be loaded
            loadImage(path);
        }
        loadImage(paths.get(index));
    }

    @Override
    public String getName() {
        return "Multi-Image";
    }

    @Override
    protected OutputSocket[] createOutputSockets() {
        return new OutputSocket[]{
                outputSocket
        };
    }

    @Override
    public Properties getProperties() {
        final Properties properties = new Properties();
        properties.setProperty(SIZE_PROPERTY, Integer.toString(paths.size()));
        properties.setProperty(INDEX_PROPERTY, Integer.toString(index));
        for (int i = 0; i < paths.size(); i++) {
            properties.setProperty(getPathProperty(i), paths.get(i));
        }
        return properties;
    }

    @Override
    public void createFromProperties(EventBus eventBus, Properties properties) throws IOException {
        final int index = Integer.valueOf(properties.getProperty(INDEX_PROPERTY));
        final int size = Integer.valueOf(properties.getProperty(SIZE_PROPERTY));
        final List<String> paths = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            paths.add(properties.getProperty(getPathProperty(i)));
        }
        this.initialize(eventBus, paths, index);
    }


    private void loadImage(String path) throws IOException {
        OpenCVUtility.loadImage(path, this.outputSocket.getValue().get());
        this.outputSocket.setValue(this.outputSocket.getValue().get());
        this.eventBus.post(new SourceStartedEvent(this));
    }


    private int getListIndex(){
        final int listSize = paths.size();
        int indexToGet = index % listSize;
        //this might happen to be negative
        return indexToGet < 0 ? indexToGet + listSize : indexToGet;
    }

    @Override
    public synchronized void nextValue() throws IOException {
        index ++;
        loadImage(paths.get(getListIndex()));
    }

    @Override
    public synchronized void previousValue() throws IOException {
        index--;
        loadImage(paths.get(getListIndex()));
    }

    private static String getPathProperty(int index) {
        return "path[" + index + "]";
    }
}
