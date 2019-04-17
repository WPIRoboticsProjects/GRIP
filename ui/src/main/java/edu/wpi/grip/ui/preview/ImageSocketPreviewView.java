package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.GripPlatform;

import javafx.scene.image.Image;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing OpenCV Mats.
 */
public class ImageSocketPreviewView extends ImageBasedPreviewView<MatWrapper> {

  private final GripPlatform platform;

  /**
   * @param socket An output socket to preview.
   */
  ImageSocketPreviewView(GripPlatform platform, OutputSocket<MatWrapper> socket) {
    super(socket);
    this.platform = platform;
    this.setContent(imageView);
  }

  @Override
  protected void convertImage() {
    synchronized (this) {
      this.getSocket().getValue()
          .filter(ImageBasedPreviewView::isPreviewable)
          .ifPresent(m -> {
            platform.runAsSoonAsPossible(() -> {
              Image image = imageConverter.convert(m.getCpu(), getImageHeight());
              imageView.setImage(image);
            });
          });
    }
  }
}
