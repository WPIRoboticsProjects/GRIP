package edu.wpi.grip.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketsProvider;

import java.io.InputStream;
import java.util.Optional;

/**
 * The common interface used by <code>Step</code>s in a pipeline to call various operations.  There is usually only one
 * instance of any class that implements <code>Operation</code>, which is called whenever that operation is used.
 */
public interface Operation {

    enum Category {
        IMAGE_PROCESSING,
        FEATURE_DETECTION,
        NETWORK,
        LOGICAL,
        OPENCV,
        MISCELLANEOUS,
    }

    /**
     * @return The unique user-facing name of the operation, such as "Gaussian Blur"
     */
    String getName();

    /**
     * @return Any old unique user-facing names of the operation. This is used to preserve compatibility with
     * old versions of GRIP if the operation name changes.
     */
    default ImmutableSet<String> getAliases() {
        return ImmutableSet.of();
    }


    /**
     * @return A description of the operation.
     */
    String getDescription();

    /**
     * @return What category the operation falls under.  This is used to organize them in the GUI
     */
    default Category getCategory() {
        return Category.MISCELLANEOUS;
    }

    /**
     * @return An {@link InputStream} of a 128x128 image to show the user as a representation of the operation.
     */
    default Optional<InputStream> getIcon() {
        return Optional.empty();
    }

    default SocketsProvider createSockets(EventBus eventBus) {
        return new SocketsProvider(createInputSockets(eventBus), createOutputSockets(eventBus));
    }
    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of sockets for the inputs that the operation expects.
     */
    InputSocket<?>[] createInputSockets(EventBus eventBus);

    /**
     * @param eventBus The Guava {@link EventBus} used by the application.
     * @return An array of sockets for the outputs that the operation produces.
     */
    OutputSocket<?>[] createOutputSockets(EventBus eventBus);

    /**
     * Override this to provide persistent per-step data
     */
    default Optional<?> createData() {
        return Optional.empty();
    }

    /**
     * Perform the operation on the specified inputs, storing the results in the specified outputs.
     *
     * @param inputs  An array obtained from {@link #createInputSockets(EventBus)}. The caller can set the value of
     *                each socket to an actual parameter for the operation.
     * @param outputs An array obtained from {@link #createOutputSockets(EventBus)}. The outputs of the operation will
     *                be stored in these sockets.
     * @param data    Optional data to be passed to the operation
     */
    default void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        perform(inputs, outputs);
    }

    default void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        throw new UnsupportedOperationException("Perform was not overridden");
    }

    /**
     * Allows the step to clean itself up when removed from the pipeline.
     * This should only be called by {@link Step#setRemoved()} to ensure correct synchronization.
     *
     * @param inputs  An array obtained from {@link #createInputSockets(EventBus)}. The caller can set the value of
     *                each socket to an actual parameter for the operation.
     * @param outputs An array obtained from {@link #createOutputSockets(EventBus)}. The outputs of the operation will
     *                be stored in these sockets.
     * @param data    Optional data to be passed to the operation
     */
    default void cleanUp(InputSocket<?>[] inputs, OutputSocket<?>[] outputs, Optional<?> data) {
        /* no-op */
    }
}
