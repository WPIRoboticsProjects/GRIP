package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.util.GripPlatform;
import edu.wpi.grip.ui.util.ImageConverter;

import com.google.common.eventbus.Subscribe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Point;
import static org.bytedeco.javacpp.opencv_core.Scalar;
import static org.bytedeco.javacpp.opencv_core.bitwise_xor;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * A SocketPreviewView for BlobsReports that shows the original image with circles overlayed onto
 * it, showing the location and size of detected blobs.
 */
public class BlobsSocketPreviewView extends SocketPreviewView<BlobsReport> {

  private final ImageConverter imageConverter = new ImageConverter();
  private final ImageView imageView = new ImageView();
  private final Label infoLabel = new Label();
  private final Mat tmp = new Mat();
  private final Point point = new Point();
  private final GripPlatform platform;
  @SuppressWarnings("PMD.ImmutableField")
  @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC",
      justification = "Do not need to synchronize inside of a constructor")
  private boolean showInputImage = false;

  /**
   * @param socket An output socket to preview.
   */
  public BlobsSocketPreviewView(GripPlatform platform, OutputSocket<BlobsReport> socket) {
    super(socket);
    this.platform = platform;
    final CheckBox show = new CheckBox("Show Input Image");
    show.setSelected(this.showInputImage);
    show.selectedProperty().addListener(observable -> {
      this.showInputImage = show.isSelected();
      this.convertImage();
    });

    final VBox content = new VBox(this.imageView, new Separator(Orientation.HORIZONTAL), this
        .infoLabel, show);
    content.getStyleClass().add("preview-box");
    this.setContent(content);

    assert Platform.isFxApplicationThread() : "Must be in FX Thread to create this or you will be"
        + " exposing constructor to another thread!";
  }


  @Subscribe
  public void onRender(RenderEvent event) {
    this.convertImage();
  }

  private void convertImage() {
    synchronized (this) {
      final BlobsReport blobsReport = this.getSocket().getValue().get();
      final Mat input = blobsReport.getInput();

      if (input.channels() == 3) {
        input.copyTo(tmp);
      } else {
        cvtColor(input, tmp, CV_GRAY2BGR);
      }
      // If we don't want to see the background image, set it to black
      if (!this.showInputImage) {
        bitwise_xor(tmp, tmp, tmp);
      }

      // If there were lines found, draw them on the image before displaying it
      if (!blobsReport.getBlobs().isEmpty()) {
        // For each line in the report, draw a line along with the starting and ending points
        for (BlobsReport.Blob blob : blobsReport.getBlobs()) {
          point.x((int) blob.x);
          point.y((int) blob.y);
          circle(tmp, point, (int) (blob.size / 2), Scalar.WHITE, 2, LINE_8, 0);
        }
      }

      final Mat output = tmp;
      final int numBlobs = blobsReport.getBlobs().size();
      platform.runAsSoonAsPossible(() -> {
        final Image image = this.imageConverter.convert(output);
        this.imageView.setImage(image);
        this.infoLabel.setText("Found " + numBlobs + " blobs");
      });
    }
  }
}
