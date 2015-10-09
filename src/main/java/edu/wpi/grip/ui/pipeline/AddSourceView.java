package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.sources.ImageFileSource;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Screen;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A box of buttons that let the user add different kinds of {@link Source}s.  Depending on which button is pressed,
 * a different dialog is presented for the user to construct that source.  As an example, the image file source results
 * in a file picker that the user can use to browse for an image.
 */
public class AddSourceView extends HBox {

    private final static double ICON_SIZE_INCHES = 1.0 / 6.0;

    private final EventBus eventBus;

    /**
     * Sources typically have to block while they load, so that should be done in a separate daemon thread to avoid
     * freezing up the GUI.
     */
    private final Executor loadSourceExecutor = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    public AddSourceView(EventBus eventBus) {
        this.eventBus = eventBus;

        this.setFillHeight(true);

        addButton("Add\nImage", getClass().getResource("/edu/wpi/grip/ui/icons/add-image.png"), mouseEvent -> {
            // Show a file picker so the user can open one or more images from disk
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open an image");

            final List<File> imageFiles = fileChooser.showOpenMultipleDialog(this.getScene().getWindow());

            if (imageFiles == null) return;

            // Add a new source for each image and load it.
            imageFiles.forEach(file -> {
                final ImageFileSource source = new ImageFileSource(eventBus);
                eventBus.post(new SourceAddedEvent(source));

                this.loadSourceExecutor.execute(() -> {
                    try {
                        source.loadImage(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                });
            });
        });

        addButton("Add\nWebcam", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            System.out.println(mouseEvent);
        });
    }

    /**
     * Add a new button for adding a source.  This method takes care of resizing the button graphic and setting the
     * event handler.
     */
    private void addButton(String text, URL graphicURL, EventHandler<? super MouseEvent> onMouseClicked) {
        final ImageView graphic = new ImageView(graphicURL.toString());
        graphic.setFitWidth(Screen.getPrimary().getDpi() * ICON_SIZE_INCHES);
        graphic.setFitHeight(Screen.getPrimary().getDpi() * ICON_SIZE_INCHES);

        final Button button = new Button(text, graphic);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnMouseClicked(onMouseClicked);

        this.getChildren().add(button);
    }
}
