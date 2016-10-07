package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.util.MatUtils;
import edu.wpi.grip.ui.util.GripPlatform;
import edu.wpi.grip.ui.util.ImageConverter;

import com.google.common.eventbus.Subscribe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

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
import static org.bytedeco.javacpp.opencv_imgproc.circle;
import static org.bytedeco.javacpp.opencv_imgproc.line;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing containing the result of a line
 * detection algorithm.
 */
public class LinesSocketPreviewView extends SocketPreviewView<LinesReport> {

  private final ImageConverter imageConverter = new ImageConverter();
  private final ImageView imageView = new ImageView();
  private final Label infoLabel = new Label();
  private final GripPlatform platform;
  @SuppressWarnings("PMD.ImmutableField")
  @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC",
                      justification = "Do not need to synchronize inside of a constructor")
  private boolean showInputImage = false;

  /**
   * @param socket An output socket to preview.
   */
  public LinesSocketPreviewView(GripPlatform platform, OutputSocket<LinesReport> socket) {
    super(socket);
    this.platform = platform;

    // Add a checkbox to set if the preview should just show the lines, or also the input image
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
    convertImage();
  }

  @Subscribe
  public void onRender(RenderEvent event) {
    this.convertImage();
  }

  private void convertImage() {
    synchronized (this) {
      final LinesReport linesReport = this.getSocket().getValue().get();
      final List<LinesReport.Line> lines = linesReport.getLines();
      Mat input = linesReport.getInput();

      input = MatUtils.draw(
          input,
          showInputImage,
          linesReport::getLines,
          (m, lr) -> lr.forEach(l -> drawLine(m, l))
      );

      final Mat convertInput = input;
      final int numLines = lines.size();
      platform.runAsSoonAsPossible(() -> {
        final Image image = this.imageConverter.convert(convertInput);
        this.imageView.setImage(image);
        this.infoLabel.setText("Found " + numLines + " lines");
      });
    }
  }

  private void drawLine(Mat image, LinesReport.Line line) {
    final Point startPoint = new Point((int) line.x1, (int) line.y1);
    final Point endPoint = new Point((int) line.x2, (int) line.y2);
    line(image, startPoint, endPoint, Scalar.WHITE, 2, LINE_8, 0);
    circle(image, startPoint, 2, Scalar.WHITE, 2, LINE_8, 0);
    circle(image, endPoint, 2, Scalar.WHITE, 2, LINE_8, 0);
  }

}
