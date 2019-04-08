package edu.wpi.grip.ui.pipeline;

import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.events.SocketPreviewChangedEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.Socket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.ui.Controller;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.preview.ImageBasedPreviewView;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that renders an {@link OutputSocket} that is the output of a step.  It shows a
 * label with the identifier of the output, as well as a handle for connections to other sockets,
 * and some buttons to configure what do to with the output.
 */
@ParametrizedController(url = "OutputSocket.fxml")
public class OutputSocketController implements Controller {

  private final SocketHandleView.Factory socketHandleFactory;
  private final InvalidationListener previewListener;
  private final OutputSocket<?> socket;
  @FXML
  private HBox root;
  @FXML
  private Label identifier;
  @FXML
  private Label type;
  @FXML
  private ToggleButton preview;
  @FXML
  private StackPane handlePane;
  /**
   * The "handle" is a simple shape next ot the socket identifier that shows whether or not there is
   * a connection going to or from the socket.  If there is such a connection, the ConnectionView is
   * rendered as a curve going from one handle to another.
   */
  private SocketHandleView handle;

  @Inject
  OutputSocketController(SocketHandleView.Factory socketHandleFactory, @Assisted OutputSocket
      socket) {
    this.socketHandleFactory = checkNotNull(socketHandleFactory, "Socket Handle factory can not "
        + "be null");
    this.socket = checkNotNull(socket, "The output socket can not be null");
    this.previewListener = value -> this.socket.setPreviewed(this.preview.isSelected());
  }

  @FXML
  @SuppressWarnings("unchecked")
  protected void initialize() {
    this.handle = socketHandleFactory.create(this.socket);

    this.handlePane.getChildren().add(this.handle);

    // Show a button to choose if we want to preview the socket or not
    this.preview.setSelected(this.socket.isPreviewed());
    this.preview.selectedProperty().addListener(previewListener);

    SocketHint<?> socketHint = this.socket.getSocketHint();

    // Set the label on the control based on the identifier from the socket hint
    this.identifier.setText(socketHint.getIdentifier());
    this.type.setText(this.socket.getSocketHint().getTypeLabel());
  }

  public Socket<?> getSocket() {
    return this.socket;
  }

  public SocketHandleView getHandle() {
    return this.handle;
  }

  @Subscribe
  public void onSocketChanged(SocketChangedEvent event) {
    if (event.isRegarding(this.socket)) {
      if (!this.socket.getValue().isPresent()) {
        // No value
        handlePreview(false);
      } else if (!(this.socket.getValue().get() instanceof MatWrapper)) {
        // There is a non-image value, which can always be previewed
        handlePreview(true);
      } else {
        // Only allow the image to be previewed if it's previewable
        boolean previewable = this.socket.getValue()
            .map(MatWrapper.class::cast)
            .map(ImageBasedPreviewView::isPreviewable)
            .get();
        handlePreview(previewable);
      }
    }
  }

  /**
   * Disables the preview button and hides the preview if the socket value isn't able to be
   * previewed.
   */
  private void handlePreview(boolean previewable) {
    this.preview.setDisable(!previewable);
    this.socket.setPreviewed(this.socket.isPreviewed() && previewable);
  }

  @Subscribe
  public void onSocketPreviewChangedEvent(SocketPreviewChangedEvent event) {
    // Only try to update the button if the two aren't the same
    // This really should only happen we deserialize the pipeline
    if (event.isRegarding(socket) && socket.isPreviewed() != preview.isSelected()) {
      preview.selectedProperty().removeListener(previewListener);
      preview.setSelected(socket.isPreviewed());
      preview.selectedProperty().addListener(previewListener);
    }
  }

  /**
   * Disable user input while benchmarking.
   */
  @Subscribe
  private void onBenchmarkEvent(BenchmarkEvent e) {
    Platform.runLater(() -> handle.setDisable(e.isStart()));
  }

  @VisibleForTesting
  ToggleButton previewButton() {
    return preview;
  }

  @Override
  public Node getRoot() {
    return root;
  }

  public interface Factory {
    OutputSocketController create(OutputSocket socket);
  }
}
