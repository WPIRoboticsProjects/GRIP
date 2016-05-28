package edu.wpi.grip.core.sources;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.math.IntMath;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import edu.wpi.grip.core.PreviousNext;
import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness;
import edu.wpi.grip.core.util.ImageLoadingUtility;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * A Source that supports multiple images. They can be toggled using {@link MultiImageFileSource#next()} and
 * {@link MultiImageFileSource#previous()}
 */
@XStreamAlias(value = "grip:MultiImageFile")
public final class MultiImageFileSource extends Source implements PreviousNext {
    private static final String INDEX_PROPERTY = "index";
    private static final String SIZE_PROPERTY = "numImages";

    private final SocketHint<Mat> imageOutputHint = SocketHints.Inputs.createMatSocketHint("Image", true);
    private final OutputSocket<Mat> outputSocket;

    private final EventBus eventBus;
    private final List<String> paths;
    private final AtomicInteger index;
    private Mat[] images;
    private Optional<Mat> currentImage = Optional.empty();

    public interface Factory {
        MultiImageFileSource create(List<File> files, int index);

        MultiImageFileSource create(List<File> files);

        MultiImageFileSource create(Properties properties);
    }

    /**
     * @param eventBus                The event bus.
     * @param exceptionWitnessFactory Factory to create the exceptionWitness
     * @param files                   A list of files to be loaded.
     * @param index                   The index to use as the first file that is in the socket.
     */
    @AssistedInject
    MultiImageFileSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final List<File> files,
            @Assisted final int index) {
        this(eventBus, outputSocketFactory, exceptionWitnessFactory, files.stream()
                .map(file -> URLDecoder.decode(Paths.get(file.toURI()).toString()))
                .collect(Collectors.toList()).toArray(new String[files.size()]), index);
    }

    @AssistedInject
    MultiImageFileSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            @Assisted final List<File> files) {
        this(eventBus, outputSocketFactory, exceptionWitnessFactory, files, 0);
    }

    /**
     * Used only for serialization
     */
    @AssistedInject
    MultiImageFileSource(final EventBus eventBus,
                         final OutputSocket.Factory outputSocketFactory,
                         final ExceptionWitness.Factory exceptionWitnessFactory,
                         @Assisted final Properties properties) {
        this(eventBus, outputSocketFactory, exceptionWitnessFactory, pathsFromProperties(properties), indexFromProperties(properties));
    }

    private MultiImageFileSource(
            final EventBus eventBus,
            final OutputSocket.Factory outputSocketFactory,
            final ExceptionWitness.Factory exceptionWitnessFactory,
            final String[] paths,
            final int index) {
        super(exceptionWitnessFactory);
        this.eventBus = eventBus;
        this.outputSocket = outputSocketFactory.create(imageOutputHint);
        this.index = new AtomicInteger(checkElementIndex(index, paths.length, "File List Index"));
        this.paths = Arrays.asList(paths);
    }

    @Override
    public void initialize() throws IOException {
        this.images = createImagesArray(this.paths);
        currentImage = Optional.of(addIndexAndGetImageByOffset(0));
        eventBus.post(new SourceHasPendingUpdateEvent(this));
    }

    @Override
    public String getName() {
        return "Multi-Image";
    }

    @Override
    protected List<OutputSocket> createOutputSockets() {
        return ImmutableList.of(
                outputSocket
        );
    }

    @Override
    protected boolean updateOutputSockets() {
        if (!currentImage.equals(outputSocket.getValue())) {
            outputSocket.setValueOptional(currentImage);
            return true;
        } else {
            return false;
        }
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
        currentImage = Optional.of(addIndexAndGetImageByOffset(+1));
        eventBus.post(new SourceHasPendingUpdateEvent(this));
    }

    /**
     * Assigns the output socket to the previous image. (Wraps around)
     */
    @Override
    public final void previous() {
        currentImage = Optional.of(addIndexAndGetImageByOffset(-1));
        eventBus.post(new SourceHasPendingUpdateEvent(this));
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

    private static int sizeFromProperties(Properties properties) {
        return Integer.parseInt(properties.getProperty(SIZE_PROPERTY));
    }

    private static int indexFromProperties(Properties properties) {
        return Integer.parseInt(properties.getProperty(INDEX_PROPERTY));
    }

    private static String[] pathsFromProperties(Properties properties) {
        final int size = sizeFromProperties(properties);
        final String[] paths = new String[size];
        for (int i = 0; i < size; i++) {
            paths[i] = (properties.getProperty(getPathProperty(i)));
        }
        return paths;
    }
}
