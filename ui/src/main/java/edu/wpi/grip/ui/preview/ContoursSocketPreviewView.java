package edu.wpi.grip.ui.preview;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.core.events.SocketChangedEvent;
import edu.wpi.grip.core.operations.composite.ContoursReport;
import edu.wpi.grip.ui.util.ImageConverter;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.drawContours;

/**
 * A preview view for displaying contours.  This view shows each contour as a different-colored outline (so they can be
 * individually distinguished), as well as a count of the total number of contours found.
 */
public final class ContoursSocketPreviewView extends SocketPreviewView<ContoursReport> {

    private final ImageConverter imageConverter = new ImageConverter();
    private final ImageView imageView = new ImageView();
    private final Label infoLabel = new Label();
    private final CheckBox colorContours = new CheckBox("Color Contours");
    private final Mat tmp = new Mat();

    private final static Scalar[] CONTOUR_COLORS = new Scalar[]{
            Scalar.RED,
            Scalar.YELLOW,
            Scalar.GREEN,
            Scalar.CYAN,
            Scalar.BLUE,
            Scalar.MAGENTA,
    };

    /**
     * @param eventBus The EventBus used by the application
     * @param socket   An output socket to preview
     */
    public ContoursSocketPreviewView(EventBus eventBus, OutputSocket<ContoursReport> socket) {
        super(eventBus, socket);

        this.setContent(new VBox(this.imageView, this.infoLabel, this.colorContours));
        this.render();

        this.colorContours.selectedProperty().addListener(observable -> this.render());
        this.colorContours.setSelected(false);
    }

    @Subscribe
    public void onSocketChanged(SocketChangedEvent event) {
        if (event.getSocket() == this.getSocket()) {
            this.render();
        }
    }

    private void render() {
        synchronized (this) {
            final ContoursReport contours = this.getSocket().getValue().get();
            long numContours = 0;

            if (!contours.getContours().isNull() && contours.getRows() > 0 && contours.getCols() > 0) {
                // Allocate a completely black OpenCV Mat to draw the contours onto.  We can easily render contours
                // by using OpenCV's drawContours function and converting the Mat into a JavaFX Image.
                this.tmp.create(contours.getRows(), contours.getCols(), CV_8UC3);
                bitwise_xor(tmp, tmp, tmp);

                numContours = contours.getContours().size();

                if (this.colorContours.isSelected()) {
                    for (int i = 0; i < numContours; i++) {
                        drawContours(this.tmp, contours.getContours(), i, CONTOUR_COLORS[i % CONTOUR_COLORS.length]);
                    }
                } else {
                    drawContours(this.tmp, contours.getContours(), -1, Scalar.WHITE);
                }
            }

            final Image image = this.imageConverter.convert(tmp);
            final long finalNumContours = numContours;
            Platform.runLater(() -> {
                synchronized (this) {
                    this.imageView.setImage(image);
                    this.infoLabel.setText("Found " + finalNumContours + " contours");
                }
            });
        }
    }
}
