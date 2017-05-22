package edu.wpi.grip.core.operations.network;

import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.Icon;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Abstract superclass for operations that publish data.
 */
public abstract class NetworkPublishOperation<D> implements Operation {

  /**
   * OperationDescription builder that has the icon default to "publish" and the category to
   * "NETWORK".
   */
  protected static final OperationDescription.Builder defaultBuilder =
      OperationDescription.builder()
          .icon(Icon.iconStream("publish"))
          .category(OperationDescription.Category.NETWORK);


  protected final Class<D> dataType;
  protected final InputSocket<D> dataSocket;
  protected final InputSocket<String> nameSocket;
  private final SocketHint<String> nameHint = SocketHints.Inputs.createTextSocketHint("Name", "");

  protected NetworkPublishOperation(InputSocket.Factory isf, Class<D> dataType) {
    checkNotNull(isf);
    checkNotNull(dataType);
    final SocketHint<D> dataHint =
        new SocketHint.Builder<>(dataType)
            .identifier("Data")
            .build();
    this.dataType = dataType;
    this.dataSocket = isf.create(dataHint, "data-type");
    this.nameSocket = isf.create(nameHint, "published-name");
  }

  @Override
  public List<InputSocket> getInputSockets() {
    return ImmutableList.<InputSocket>builder()
        .add(dataSocket)
        .add(nameSocket)
        .addAll(createFlagSockets())
        .build();
  }

  /**
   * Creates a list of input sockets that control which items to publish.
   */
  protected abstract List<InputSocket<Boolean>> createFlagSockets();

  @Override
  public List<OutputSocket> getOutputSockets() {
    return ImmutableList.of();
  }

  /**
   * Publishes the data.
   */
  protected abstract void doPublish();

  @Override
  public void perform() {
    doPublish();
  }
}
