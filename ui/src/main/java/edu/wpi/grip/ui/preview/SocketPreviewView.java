package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.sockets.OutputSocket;

import javafx.scene.control.TitledPane;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A JavaFX control that shows a preview of the current value of a socket.  This allows the user to
 * have real-time feedback on an algorithm.
 */
public abstract class SocketPreviewView<T> extends TitledPane {
  private final OutputSocket<T> socket;

  /**
   * @param socket An output socket to preview.
   */
  protected SocketPreviewView(OutputSocket<T> socket) {
    this.socket = checkNotNull(socket);

    this.setText(this.getTitle());
    this.getStyleClass().add("socket-preview");
    this.setCollapsible(false);
  }

  /**
   * A string of text to display in the GUI to identify this preview, including the name of the
   * output socket and what operation it is the output of.
   *
   * @return The title for the socket preview view.
   */
  private String getTitle() {
    StringBuilder title = new StringBuilder();

    if (this.socket.getStep().isPresent()) {
      title.append(this.socket.getStep().get().getOperationDescription().name()).append(" -> ");
    }

    title.append(this.socket.getSocketHint().getIdentifier());

    return title.toString();
  }

  public OutputSocket<T> getSocket() {
    return this.socket;
  }

}
