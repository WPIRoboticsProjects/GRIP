package edu.wpi.grip.core;

import com.google.inject.Inject;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * Base class for an input into the pipeline.
 */
public abstract class Source {

    public static class SourceFactoryImpl implements SourceFactory {
        @Inject
        CameraSource.Factory cameraFactory;
        @Inject
        ImageFileSource.Factory imageFactory;
        @Inject
        MultiImageFileSource.Factory multiImageFactory;

        @Override
        public Source create(Class type, Properties properties) throws IOException {
            if(type.isAssignableFrom(CameraSource.class)) return cameraFactory.create(properties);
            else if(type.isAssignableFrom(ImageFileSource.class)) return imageFactory.create(properties);
            else if(type.isAssignableFrom(MultiImageFileSource.class)) return multiImageFactory.create(properties);
            else throw new IllegalArgumentException(type + " was not a valid type");
        }
    }

    public interface SourceFactory {
        Source create(Class<?> type, Properties properties) throws IOException;
    }

    /**
     * @return The name of this source.  This is used by the GUI to distinguish different sources.  For example,
     * {@link edu.wpi.grip.core.sources.ImageFileSource} returns the filename of the image.
     */
    public abstract String getName();

    /**
     * Get the sockets for this source.
     *
     * @return @return An array of {@link OutputSocket}s for the outputs that the source produces.
     */
    public final OutputSocket[] getOutputSockets() {
        final OutputSocket[] outputSockets = this.createOutputSockets();
        for (OutputSocket socket : outputSockets) {
            socket.setSource(Optional.of(this));
        }

        return outputSockets;
    }

    protected abstract OutputSocket[] createOutputSockets();

    /**
     * @return A {@link Properties} containing data that can be used to re-create this source.  This is used for
     * serialization/deserialization.
     */
    public abstract Properties getProperties();
}
