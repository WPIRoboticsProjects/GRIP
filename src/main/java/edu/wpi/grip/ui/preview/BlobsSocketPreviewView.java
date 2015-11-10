package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.operations.composite.BlobsReport;
import edu.wpi.grip.ui.util.ImageConverter;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * A SocketPreviewView for BlobsReports that shows the original image with circles overlayed onto it,
 * showing the location and size of detected blobs.
 */
public class BlobsSocketPreviewView extends SocketPreviewView<BlobsReport> {

    private final ImageConverter imageConverter = new ImageConverter();
    private final ImageView imageView = new ImageView();
    private final Label infoLabel = new Label();
    private final Mat tmp = new Mat();

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public BlobsSocketPreviewView(EventBus eventBus, OutputSocket<BlobsReport> socket) {
        super(eventBus, socket);

        final VBox content = new VBox();
        content.getStyleClass().add("preview-box");
        content.getChildren().add(this.imageView);
        content.getChildren().add(new Separator(Orientation.HORIZONTAL));
        content.getChildren().add(this.infoLabel);
        this.setContent(content);

        this.convertImage();
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.convertImage();
        }
    }

    private void convertImage() {
        synchronized (this) {
            final BlobsReport blobsReport = this.getSocket().getValue();
            Mat input = blobsReport.getInput();

            // If there were lines found, draw them on the image before displaying it
            if (!blobsReport.getBlobs().isEmpty()) {
                if (input.channels() == 3) {
                    input = input.clone();
                } else {
                    cvtColor(input, tmp, CV_GRAY2BGR);
                    input = tmp;
                }

                // For each line in the report, draw a line along with the starting and ending points
                for (BlobsReport.Blob blob : blobsReport.getBlobs()) {
                    final Point point = new Point((int) blob.x, (int) blob.y);
                    circle(input, point, (int) (blob.size / 2), Scalar.WHITE, 2, LINE_8, 0);
                }
            }

            final Image image = this.imageConverter.convert(input);
            final int numBlobs = blobsReport.getBlobs().size();

            Platform.runLater(() -> {
                synchronized (this) {
                    this.imageView.setImage(image);
                    this.infoLabel.setText("Found " + numBlobs + " blobs");
                }
            });
        }
    }
}
