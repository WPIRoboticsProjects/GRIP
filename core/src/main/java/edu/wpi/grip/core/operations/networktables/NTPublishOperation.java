package edu.wpi.grip.core.operations.networktables;

import com.google.common.base.Throwables;
import com.google.common.eventbus.EventBus;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.grip.core.*;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An operation that publishes any type that implements {@link NTPublishable} to NetworkTables.
 * <p>
 * To be publishable, a type should have one or more accessor methods annotated with {@link NTValue}.  This is done
 * with annotations instead of methods
 */
public class NTPublishOperation<S, T extends NTPublishable> implements Operation {

    private final Class<S> type;
    private final Function<S, T> converter;
    private final List<Method> ntValueMethods = new ArrayList<>();

    /**
     * Create a new publish operation for a socket type that implements {@link NTPublishable} directly
     */
    @SuppressWarnings("unchecked")
    public NTPublishOperation(Class<T> type) {
        this((Class<S>) type, type, value -> (T) value);
    }

    /**
     * Create a new publish operation where the socket type and NTPublishable type are different.  This is useful for
     * classes that we don't create, such as JavaCV's {@link org.bytedeco.javacpp.opencv_core.Size} class, since we
     * can't make them implement additional interfaces.
     *
     * @param socketType The type of socket that can be connected to this step
     * @param reportType A class implementing {@link NTPublishable} that determines what data is sent to NetworkTables
     * @param converter  A function to convert socket values into publishable values
     */
    public NTPublishOperation(Class<S> socketType, Class<T> reportType, Function<S, T> converter) {
        this.type = checkNotNull(socketType, "Type was null");
        this.converter = checkNotNull(converter, "Converter was null");

        // Any accessor method with an @NTValue annotation can be published to NetworkTables.
        for (Method method : reportType.getDeclaredMethods()) {
            if (method.getAnnotation(NTValue.class) != null) {
                if (method.getParameters().length > 0) {
                    throw new IllegalArgumentException("@NTValue method must have 0 parameters: " + method);
                }

                ntValueMethods.add(method);
            }
        }
    }

    @Override
    public String getName() {
        return "Publish " + type.getSimpleName();
    }

    @Override
    public String getDescription() {
        return "Publish a " + type.getSimpleName() + " to NetworkTables";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/first.png"));
    }

    @Override
    public InputSocket<?>[] createInputSockets(EventBus eventBus) {
        final InputSocket<?>[] sockets = new InputSocket[2 + ntValueMethods.size()];
        int i = 0;

        // Create an input for the actual object being published
        sockets[i++] = new InputSocket<>(eventBus,
                new SocketHint.Builder<>(type).identifier("Value").initialValue(null).build());

        // Create a string input for the key used by NetworkTables
        sockets[i++] = new InputSocket<>(eventBus,
                SocketHints.Inputs.createTextSocketHint("Subtable Name", "my" + type.getSimpleName()));

        // Create a checkbox for every property of the object that might be published.  For example, for a
        // ContourReport, the user might wish to publish the x and y coordinates of the center of each contour.
        for (Method method : ntValueMethods) {
            sockets[i++] = new InputSocket<>(eventBus,
                    SocketHints.createBooleanSocketHint("Publish " + method.getAnnotation(NTValue.class).key(), true));
        }

        return sockets;
    }

    @Override
    public OutputSocket<?>[] createOutputSockets(EventBus eventBus) {
        return new OutputSocket<?>[0];
    }

    @Override
    public void perform(InputSocket<?>[] inputs, OutputSocket<?>[] outputs) {
        int i = 0;

        final NTPublishable value = converter.apply((S) inputs[i++].getValue().get());
        final String subtableName = (String) inputs[i++].getValue().get();

        if (subtableName.isEmpty()) {
            throw new IllegalArgumentException("Need key to publish to NetworkTables");
        }

        // Get a subtable to put the values in.  Each NTPublishable has multiple properties that are published (such as
        // x, y, width, height, etc...), so they're grouped together in a subtable.
        final ITable subtable;
        synchronized (NetworkTable.class) {
            subtable = NetworkTable.getTable("GRIP").getSubTable(subtableName);
        }

        // For each NTValue method in the object being published, put it in the table if the the corresponding
        // checkbox is selected.
        try {
            for (Method method : ntValueMethods) {
                String key = method.getAnnotation(NTValue.class).key();
                if ((Boolean) inputs[i++].getValue().get()) {
                    subtable.putValue(key, method.invoke(value));
                } else {
                    subtable.delete(key);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwables.propagate(e);
        }
    }
}
