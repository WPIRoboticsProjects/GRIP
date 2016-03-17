package edu.wpi.grip.core.operations.network.networktables;

import com.google.common.collect.ImmutableSet;
import edu.wpi.grip.core.operations.network.*;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 * An operation that publishes any type that implements {@link Publishable} to NetworkTables.
 * <p>
 * To be publishable, a type should have one or more accessor methods annotated with {@link PublishValue}.  This is done
 * with annotations instead of methods
 */
public abstract class NTPublishAnnotatedOperation<S, T extends Publishable, P> extends PublishAnnotatedOperation<S, T, P> {

    /**
     * Create a new publish operation for a socket type that implements {@link Publishable} directly
     */
    @SuppressWarnings("unchecked")
    protected NTPublishAnnotatedOperation(MapNetworkPublisherFactory factory) {
        super(factory);
    }

    /**
     * Create a new publish operation where the socket type and Publishable type are different.  This is useful for
     * classes that we don't create, such as JavaCV's {@link org.bytedeco.javacpp.opencv_core.Size} class, since we
     * can't make them implement additional interfaces.
     *
     * @param converter  A function to convert socket values into publishable values
     */
    protected NTPublishAnnotatedOperation(MapNetworkPublisherFactory factory, Function<S, T> converter) {
        super(factory, converter);
    }

    @Override
    public ImmutableSet<String> getAliases() {
        return ImmutableSet.of("Publish " + getSocketType().getRawType().getSimpleName());
    }

    @Override
    public String getNetworkProtocolNameAcronym() {
        return "NT";
    }

    @Override
    public String getNetworkProtocolName() {
        return "NetworkTables";
    }

    @Override
    protected String getSocketHintStringPrompt() {
        return "Subtable Name";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/first.png"));
    }

}
