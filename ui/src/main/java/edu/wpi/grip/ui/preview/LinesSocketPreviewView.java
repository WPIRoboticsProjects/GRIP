package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.RenderEvent;
import edu.wpi.grip.core.operations.composite.LinesReport;
import edu.wpi.grip.ui.util.GRIPPlatform;
import edu.wpi.grip.ui.util.ImageConverter;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.bytedeco.javacpp.opencv_core;

import java.util.List;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * A <code>SocketPreviewView</code> that previews sockets containing containing the result of a line detection
 * algorithm
 */
public class LinesSocketPreviewView extends SocketPreviewView<LinesReport> {

    private final ImageConverter imageConverter = new ImageConverter();
    private final ImageView imageView = new ImageView();
    private final Label infoLabel = new Label();
    private final Mat tmp = new Mat();
    private final GRIPPlatform platform;
    private boolean showInputImage = false;

    /**
     * @param socket   An output socket to preview
     */
    public LinesSocketPreviewView(GRIPPlatform platform, OutputSocket<LinesReport> socket) {
        super(socket);
        this.platform = platform;

        // Add a checkbox to set if the preview should just show the lines, or also the input image
        final CheckBox show = new CheckBox("Show Input Image");
        show.setSelected(this.showInputImage);
        show.selectedProperty().addListener(observable -> {
            this.showInputImage = show.isSelected();
            this.convertImage();
        });

        final VBox content = new VBox(this.imageView, new Separator(Orientation.HORIZONTAL), this.infoLabel, show);
        content.getStyleClass().add("preview-box");
        this.setContent(content);

        assert Platform.isFxApplicationThread() : "Must be in FX Thread to create this or you will be exposing constructor to another thread!";
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

            // If there were lines found, draw them on the image before displaying it
            if (!linesReport.getLines().isEmpty()) {
                if (input.channels() == 3) {
                    input.copyTo(tmp);
                } else {
                    cvtColor(input, tmp, CV_GRAY2BGR);
                }

                input = tmp;

                // If we don't want to see the background image, set it to black
                if (!this.showInputImage) {
                    bitwise_xor(tmp, tmp, tmp);
                }

                // For each line in the report, draw a line along with the starting and ending points
                for (LinesReport.Line line : lines) {
                    final opencv_core.Point startPoint = new Point((int) line.x1, (int) line.y1);
                    final opencv_core.Point endPoint = new Point((int) line.x2, (int) line.y2);
                    line(input, startPoint, endPoint, Scalar.WHITE, 2, LINE_8, 0);
                    circle(input, startPoint, 2, Scalar.WHITE, 2, LINE_8, 0);
                    circle(input, endPoint, 2, Scalar.WHITE, 2, LINE_8, 0);
                }
            }
            final Mat convertInput = input;
            final int numLines = lines.size();
            platform.runAsSoonAsPossible(() -> {
                final Image image = this.imageConverter.convert(convertInput);
                this.imageView.setImage(image);
                this.infoLabel.setText("Found " + numLines + " lines");
            });
        }
    }
}
