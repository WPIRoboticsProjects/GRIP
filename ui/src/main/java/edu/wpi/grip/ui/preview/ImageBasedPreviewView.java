package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.ImageConverter;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.scene.image.ImageView;

/**
 * Base class for image previews.
 */
public abstract class ImageBasedPreviewView<T> extends SocketPreviewView<T> {

  /**
   * Image converter for converting OpenCV mats to JavaFX images.
   */
  protected final ImageConverter imageConverter = new ImageConverter();

  /**
   * The view showing the image.
   */
  protected final ImageView imageView = new ImageView();

  private int imageHeight = 1;

  /**
   * @param socket An output socket to preview.
   */
  protected ImageBasedPreviewView(OutputSocket<T> socket) {
    super(socket);
    assert Platform.isFxApplicationThread() : "Must be in FX Thread to create this or you will be"
        + " exposing constructor to another thread!";
  }

  /**
   * Gets the height of the image to render.
   */
  protected final int getImageHeight() {
    return imageHeight;
  }

  /**
   * Converts the input data to an image and render it in the {@link #imageView}.
   */
  protected abstract void convertImage();

  /**
   * Updates the image preview when the pipeline runs.
   */
  @Subscribe
  public final void onRenderEvent(RenderEvent e) {
    convertImage();
  }

  /**
   * Resizes the image based on the given height while preserving the ratio.
   */
  public final void resize(int imageHeight) {
    this.imageHeight = imageHeight;
    convertImage();
  }

}
