package edu.wpi.grip.core.operations.network.http;

import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.PublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.util.Icon;

import java.util.function.Function;

/**
 * An operation for publishing data to the internal HTTP server, from which which remote
 * applications can request the data.
 *
 * @see edu.wpi.grip.core.http.GripServer
 */
public class HttpPublishOperation<D, P extends Publishable>
    extends PublishAnnotatedOperation<D, P> {

  @SuppressWarnings("unchecked")
  public HttpPublishOperation(InputSocket.Factory isf,
                              Class<P> dataType,
                              MapNetworkPublisherFactory factory) {
    this(isf, (Class<D>) dataType, dataType, d -> (P) d, factory);
  }

  public HttpPublishOperation(InputSocket.Factory isf,
                              Class<D> dataType,
                              Class<P> publishType,
                              Function<D, P> converter,
                              MapNetworkPublisherFactory factory) {
    super(isf, dataType, publishType, converter, factory);
    super.nameSocket.setValue("my" + dataType.getSimpleName());
  }

  /**
   * Gets a description for an {@code HttpPublishOperation} that publishes the given data type.
   *
   * @param dataType the type of the data published by the {@code HttpPublishOperation} for the data
   *                 type described
   * @return a description for an {@code HttpPublishOperation} that publishes the given data type
   */
  public static OperationDescription descriptionFor(Class<?> dataType) {
    return OperationDescription.builder()
        .name("HTTP Publish " + dataType.getSimpleName())
        .summary("Publishes a " + dataType.getSimpleName() + " to the internal HTTP server")
        .icon(Icon.iconStream("publish"))
        .category(OperationDescription.Category.NETWORK)
        .build();
  }
}
