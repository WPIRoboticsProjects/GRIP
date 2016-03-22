package edu.wpi.grip.core.operations.network.http;

import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.PublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.Publishable;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
public abstract class HttpPublishOperation<S, T extends Publishable, P> extends PublishAnnotatedOperation<S, T, P> {

    protected HttpPublishOperation(MapNetworkPublisherFactory factory) {
        super(factory);
    }

    protected HttpPublishOperation(MapNetworkPublisherFactory factory, Function<S, T> converter) {
        super(factory, converter);
    }

    @Override
    protected String getNetworkProtocolNameAcronym() {
        return "HTTP";
    }

    @Override
    protected String getNetworkProtocolName() {
        return "HTTP";
    }

    @Override
    protected String getSocketHintStringPrompt() {
        return "Topic";
    }

    @Override
    public Optional<InputStream> getIcon() {
        return Optional.of(getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/publish.png"));
    }
}
