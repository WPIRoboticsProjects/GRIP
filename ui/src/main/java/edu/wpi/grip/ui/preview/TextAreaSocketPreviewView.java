package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.GripPlatform;

import com.google.common.eventbus.Subscribe;

import javafx.scene.control.TextArea;

/**
 * A <code>SocketPreviewView</code> that previews sockets by simply calling their
 * <code>toString()</code> method and showing the result in a text area.
 */
public class TextAreaSocketPreviewView<T> extends SocketPreviewView<T> {

  private final TextArea text;
  private final GripPlatform platform;

  /**
   * @param socket An output socket to preview.
   */
  public TextAreaSocketPreviewView(GripPlatform platform, OutputSocket<T> socket) {
    super(socket);
    this.platform = platform;

    this.setStyle("-fx-pref-width: 20em;");

    this.text = new TextArea(socket.getValue().orElse((T) "").toString());
    text.setEditable(false);

    this.setContent(text);
  }

  @Subscribe
  public void onRender(RenderEvent event) {
    platform.runAsSoonAsPossible(() -> {
      this.text.setText(getSocket().getValue().orElse((T) "").toString());
    });
  }
}
