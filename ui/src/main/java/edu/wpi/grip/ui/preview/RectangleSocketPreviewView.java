package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.operations.composite.RectsReport;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.GripPlatform;

import java.util.List;

import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

/**
 * Preview for a rectangle report socket.
 */
public final class RectangleSocketPreviewView extends ImageBasedPreviewView<RectsReport> {

  private final Label infoLabel = new Label();
  private final Mat tmp = new Mat();
  private final GripPlatform platform;
  @SuppressWarnings("PMD.ImmutableField")
  private boolean showInputImage = false;

  protected RectangleSocketPreviewView(GripPlatform platform, OutputSocket<RectsReport> socket) {
    super(socket);
    this.platform = platform;

    // Add a checkbox to set if the preview should just show the rectangles, or also the input image
    final CheckBox show = new CheckBox("Show Input Image");
    show.setSelected(this.showInputImage);
    show.selectedProperty().addListener(observable -> {
      synchronized (this) {
        this.showInputImage = show.isSelected();
        this.convertImage();
      }
    });

    final VBox content = new VBox(this.imageView, new Separator(Orientation.HORIZONTAL), this
        .infoLabel, show);
    content.getStyleClass().add("preview-box");
    this.setContent(content);
  }

  @Override
  protected void convertImage() {
    synchronized (this) {
      final RectsReport report = this.getSocket().getValue().get();
      final List<Rect> rectangles = report.getRectangles();
      Mat input = report.getImage();

      if (input.channels() == 3) {
        input.copyTo(tmp);
      } else {
        cvtColor(input, tmp, CV_GRAY2BGR);
      }

      // If we don't want to see the background image, set it to black
      if (!this.showInputImage) {
        bitwise_xor(tmp, tmp, tmp);
      }

      // If rectangles were found, draw them on the image before displaying it
      for (Rect r : rectangles) {
        rectangle(tmp, r, Scalar.WHITE, 3, LINE_8, 0);
      }
      final Mat convertInput = tmp;
      final int numRegions = rectangles.size();
      platform.runAsSoonAsPossible(() -> {
        final Image image = this.imageConverter.convert(convertInput, getImageHeight());
        this.imageView.setImage(image);
        this.infoLabel.setText("Found " + numRegions + " regions of interest");
      });
    }
  }


}
