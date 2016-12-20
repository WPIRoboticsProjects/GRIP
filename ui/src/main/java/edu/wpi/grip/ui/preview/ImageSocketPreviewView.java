package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.GripPlatform;

import javafx.scene.image.Image;

import static org.bytedeco.javacpp.opencv_core.Mat;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing OpenCV Mats.
 */
public class ImageSocketPreviewView extends ImageBasedPreviewView<Mat> {

  private final GripPlatform platform;

  /**
   * @param socket An output socket to preview.
   */
  ImageSocketPreviewView(GripPlatform platform, OutputSocket<Mat> socket) {
    super(socket);
    this.platform = platform;
    this.setContent(imageView);
  }

  @Override
  protected void convertImage() {
    synchronized (this) {
      this.getSocket().getValue().ifPresent(mat -> {
        platform.runAsSoonAsPossible(() -> {
          Image image = imageConverter.convert(mat, getImageHeight());
          imageView.setImage(image);
        });
      });
    }
  }
}
