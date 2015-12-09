package edu.wpi.grip.core.sources;


import com.google.common.eventbus.EventBus;
import com.google.common.math.IntMath;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.*;
import edu.wpi.grip.core.util.ImageLoadingUtility;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Source that supports multiple images. They can be toggled using {@link MultiImageFileSource#next()} and
 * {@link MultiImageFileSource#previous()}
 */
@XStreamAlias(value = "grip:MultiImageFile")
public final class MultiImageFileSource extends Source implements PreviousNext {
    private static final String INDEX_PROPERTY = "index";
    private static final String SIZE_PROPERTY = "numImages";

    private final SocketHint<Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private OutputSocket<Mat> outputSocket;

    private EventBus eventBus;
    private List<String> paths;
    private Mat[] images;
    private AtomicInteger index;

    /**
     * @param eventBus The event bus.
     * @param files    A list of files to be loaded.
     * @param index    The index to use as the first file that is in the socket.
     * @throws IOException If the source fails to load any of the images
     */
    public MultiImageFileSource(final EventBus eventBus, final List<File> files, int index) throws IOException {
        super();
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
    public MultiImageFileSource() {
        super();
    }


    @SuppressWarnings("unchecked")
    private void initialize(final EventBus eventBus, final List<String> paths, int index) throws IOException {
        this.eventBus = checkNotNull(eventBus, "Event bus can not be null");
        this.paths = checkNotNull(paths, "The paths can not be null");
        this.outputSocket = new OutputSocket(eventBus, imageOutputHint);
        this.index = new AtomicInteger(checkElementIndex(index, paths.size(), "File List Index"));
        this.images = createImagesArray(paths);
        this.outputSocket.setValue(addIndexAndGetImageByOffset(0));
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
        properties.setProperty(INDEX_PROPERTY, Integer.toString(index.get()));
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

    /**
     * Adds the delta to the index value and returns the matrix at that index (Circular).
     * If the delta moves the index pointer outside of the bounds of the image array the number will
     * 'overflow' to remain within the bounds of the image array.
     *
     * @param delta the value to add to the index when getting the image
     * @return The matrix at the given index in the array.
     */
    private Mat addIndexAndGetImageByOffset(final int delta) {
        final int listSize = images.length;
        final int newMatIndex = index.updateAndGet(currentIndex -> {
            assert currentIndex >= 0 : "The current index should never be less than zero";
            assert currentIndex < listSize : "The current index should always be less than the size of the list";
            // No need to do any more calculations because there is no change.
            if (delta == 0) return currentIndex;
            return IntMath.mod(currentIndex + delta, listSize);
        });
        return images[newMatIndex];
    }

    /**
     * Assigns the output socket to the next image. (Wraps around)
     */
    @Override
    public final void next() {
        outputSocket.setValue(addIndexAndGetImageByOffset(+1));
    }

    /**
     * Assigns the output socket to the previous image. (Wraps around)
     */
    @Override
    public final void previous() {
        outputSocket.setValue(addIndexAndGetImageByOffset(-1));
    }

    private static String getPathProperty(int index) {
        return "path[" + index + "]";
    }

    /**
     * Creates an array of mats from the paths on the filesystem.
     * This pre-loads them into memory so that they can be accessed quickly.
     *
     * @param paths The paths of all of the images.
     * @return The list of Mats loaded from the file system. This array will have the same number of elements as paths.
     * @throws IOException if one of the images fails to load
     */
    private static Mat[] createImagesArray(List<String> paths) throws IOException {
        final Mat[] images = new Mat[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            final Mat image = new Mat();
            ImageLoadingUtility.loadImage(paths.get(i), image);
            images[i] = image;
        }
        return images;
    }
}
