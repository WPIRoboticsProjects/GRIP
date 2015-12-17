package edu.wpi.grip.ui.pipeline;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A box of buttons that let the user add different kinds of {@link edu.wpi.grip.core.Source Source}s.  Depending on which button is pressed,
 * a different dialog is presented for the user to construct that source.  As an example, the image file source results
 * in a file picker that the user can use to browse for an image.
 */
public class AddSourceView extends HBox {

    private final EventBus eventBus;
    private final MultiImageFileSource.Factory multiImageSourceFactory;
    private final ImageFileSource.Factory imageSourceFactory;
    private final CameraSource.Factory cameraSourceFactory;

    @FunctionalInterface
    private interface SupplierWithIO<T> {
        T getWithIO() throws IOException;
    }

    private class SourceDialog extends Dialog<ButtonType> {
        private final Text errorText = new Text();

        private SourceDialog(final Parent root, Control inputField) {
            super();
            final GridPane gridContent = new GridPane();
            gridContent.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(inputField, Priority.ALWAYS);
            GridPane.setHgrow(errorText, Priority.NEVER);
            errorText.wrappingWidthProperty().bind(inputField.widthProperty());
            gridContent.add(errorText, 0, 0);
            gridContent.add(inputField, 0, 1);

            getDialogPane().setContent(gridContent);
            getDialogPane().setStyle(root.getStyle());
            getDialogPane().getStylesheets().addAll(root.getStylesheets());
            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        }
    }

    public interface Factory {
        AddSourceView create();
    }

    @Inject
    AddSourceView(EventBus eventBus,
                  MultiImageFileSource.Factory multiImageSourceFactory,
                  ImageFileSource.Factory imageSourceFactory,
                  CameraSource.Factory cameraSourceFactory) {
        this.eventBus = eventBus;
        this.multiImageSourceFactory = multiImageSourceFactory;
        this.imageSourceFactory = imageSourceFactory;
        this.cameraSourceFactory = cameraSourceFactory;

        this.setFillHeight(true);

        addButton("Add\nImage(s)", getClass().getResource("/edu/wpi/grip/ui/icons/add-image.png"), mouseEvent -> {
            // Show a file picker so the user can open one or more images from disk
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open an image");

            final List<File> imageFiles = fileChooser.showOpenMultipleDialog(this.getScene().getWindow());

            if (imageFiles == null) return;

            // Add a new source for each image .
            if (imageFiles.size() == 1) {
                try {
                    final ImageFileSource imageFileSource = imageSourceFactory.create(imageFiles.get(0));
                    imageFileSource.load();
                    eventBus.post(new SourceAddedEvent(imageFileSource));
                } catch (IOException e) {
                    eventBus.post(new UnexpectedThrowableEvent(e, "The image selected was invalid"));
                }
            } else {
                try {
                    final MultiImageFileSource multiImageFileSource = multiImageSourceFactory.create(imageFiles);
                    multiImageFileSource.load();
                    eventBus.post(new SourceAddedEvent(multiImageFileSource));
                } catch (IOException e) {
                    eventBus.post(new UnexpectedThrowableEvent(e, "One of the images selected was invalid"));
                }
            }
        });

        addButton("Add\nWebcam", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            final Parent root = this.getScene().getRoot();

            // Show a dialog for the user to pick a camera index
            final Spinner<Integer> cameraIndex = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
            final SourceDialog dialog = new SourceDialog(root, cameraIndex);

            dialog.setTitle("Add Webcam");
            dialog.setHeaderText("Choose a camera");
            dialog.setContentText("index");

            // If the user clicks OK, add a new camera source
            loadCamera(dialog,
                    () -> {
                        final CameraSource cameraSource = cameraSourceFactory.create(cameraIndex.getValue());
                        cameraSource.start();
                        return cameraSource;
                    },
                    e -> {
                        dialog.errorText.setText(e.getMessage());
                    });
        });

        addButton("Add IP\nCamera", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            final Parent root = this.getScene().getRoot();

            // Show a dialog for the user to pick a camera URL

            final TextField cameraAddress = new TextField();
            final SourceDialog dialog = new SourceDialog(root, cameraAddress);
            cameraAddress.setPromptText("Ex: http://10.1.90.11/mjpg/video.mjpg");
            cameraAddress.textProperty().addListener(observable -> {
                boolean validURL = true;

                try {
                    new URL(cameraAddress.getText()).toURI();
                } catch (MalformedURLException | URISyntaxException e) {
                    validURL = false;
                }

                // Enable the "OK" button only if the user has entered a valid URL
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!validURL);
            });

            dialog.setTitle("Add IP Camera");
            dialog.setHeaderText("Enter the IP camera URL");
            dialog.setContentText("URL");
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

            // If the user clicks OK, add a new camera source
            loadCamera(dialog,
                    () -> {
                        final CameraSource cameraSource = cameraSourceFactory.create(cameraAddress.getText());
                        cameraSource.start();
                        return cameraSource;
                    },
                    e -> {
                        dialog.errorText.setText(e.getMessage());
                    });
        });
    }

    /**
     * @param dialog               The dialog to load the camera with
     * @param cameraSourceSupplier The supplier that will create the camera
     * @param failureCallback      The handler for when the camera source supplier throws an IO Exception
     */
    private void loadCamera(Dialog<ButtonType> dialog, SupplierWithIO<CameraSource> cameraSourceSupplier, Consumer<IOException> failureCallback) {
        assert Platform.isFxApplicationThread() : "Should only run in FX thread";
        dialog.showAndWait().filter(Predicate.isEqual(ButtonType.OK)).ifPresent(result -> {
            try {
                // Will try to create the camera with the values from the supplier
                final CameraSource source = cameraSourceSupplier.getWithIO();
                eventBus.post(new SourceAddedEvent(source));
            } catch (IOException e) {
                // This will run it again with the new values retrieved by the supplier
                failureCallback.accept(e);
                loadCamera(dialog, cameraSourceSupplier, failureCallback);
            }
        });
    }


    /**
     * Add a new button for adding a source.  This method takes care of setting the event handler.
     */
    private void addButton(String text, URL graphicURL, EventHandler<? super MouseEvent> onMouseClicked) {
        final ImageView graphic = new ImageView(graphicURL.toString());
        graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
        graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

        final Button button = new Button(text, graphic);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnMouseClicked(onMouseClicked);

        this.getChildren().add(button);
    }
}
