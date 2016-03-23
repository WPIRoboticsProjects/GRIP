package edu.wpi.grip.ui.pipeline;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.common.net.InetAddresses;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.ui.util.DPIUtility;
import edu.wpi.grip.ui.util.SupplierWithIO;
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
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A box of buttons that let the user add different kinds of {@link edu.wpi.grip.core.Source Source}s.  Depending on which button is pressed,
 * a different dialog is presented for the user to construct that source.  As an example, the image file source results
 * in a file picker that the user can use to browse for an image.
 */
public class AddSourceView extends HBox {

    @VisibleForTesting
    static final String SOURCE_DIALOG_STYLE_CLASS = "source-dialog";
    private final EventBus eventBus;
    private final MultiImageFileSource.Factory multiImageSourceFactory;
    private final ImageFileSource.Factory imageSourceFactory;
    private final CameraSource.Factory cameraSourceFactory;

    private final Button webcamButton;
    private final Button ipcamButton;
    private Optional<Dialog> activeDialog = Optional.empty();

    private static class WebCamSourceDialog extends Dialog<ButtonType> {
        private final Text errorText = new Text();
        final Spinner<Integer> cameraIndex;
        final Spinner<Double> exposure;

        private WebCamSourceDialog(final Parent root) {
            super();

            this.getDialogPane().getStyleClass().add(SOURCE_DIALOG_STYLE_CLASS);

            setTitle("Add Webcam");
            setHeaderText("Choose a camera");
            setContentText("index");

            cameraIndex = new Spinner<>(0, Integer.MAX_VALUE, 0);
            exposure = new Spinner<>(-1, 4096, -1, 1);

            final GridPane gridContent = new GridPane();
            gridContent.setMaxWidth(Double.MAX_VALUE);

            GridPane.setHgrow(cameraIndex, Priority.ALWAYS);
            GridPane.setHgrow(errorText, Priority.NEVER);

            gridContent.add(new Text("Camera Index: "), 0, 0);
            gridContent.add(cameraIndex, 1, 0);
            gridContent.add(new Text("Exposure: "), 0, 1);
            gridContent.add(exposure, 1, 1);
            gridContent.add(errorText, 0, 3);

            DialogPane dialogPane = getDialogPane();
            dialogPane.setContent(gridContent);
            dialogPane.setStyle(root.getStyle());
            dialogPane.getStylesheets().addAll(root.getStylesheets());
            dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            dialogPane.lookupButton(ButtonType.OK).requestFocus();
        }

        int getCameraIndex() {
            return cameraIndex.getValue();
        }

        double getExposure() {
            return cameraIndex.getValue();
        }
    }

    private static class IPCamSourceSourceDialog extends Dialog<ButtonType> {
        private final Text errorText = new Text();
        private final TextField cameraAddress;

        private IPCamSourceSourceDialog(final Parent root) {
            super();

            this.getDialogPane().getStyleClass().add(SOURCE_DIALOG_STYLE_CLASS);

            setTitle("Add IP Camera");
            setHeaderText("Enter the IP camera URL");
            setContentText("URL");

            cameraAddress = new TextField();
            cameraAddress.setPromptText("Ex: http://10.1.90.11/mjpg/video.mjpg");
            cameraAddress.textProperty().addListener(observable -> {
                boolean validURL = true;

                try {
                    new URL(cameraAddress.getText()).toURI();
                } catch (MalformedURLException e) {
                    validURL = false;
                    if (InetAddresses.isInetAddress(cameraAddress.getText()) || cameraAddress.getText().endsWith(".local")) {
                        cameraAddress.setText("http://" + cameraAddress.getText());
                        validURL = true;
                    }
                } catch (URISyntaxException e) {
                    validURL = false;
                }

                // Enable the "OK" button only if the user has entered a valid URL
                IPCamSourceSourceDialog.this.getDialogPane().lookupButton(ButtonType.OK).setDisable(!validURL);
            });

            final GridPane gridContent = new GridPane();
            gridContent.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(cameraAddress, Priority.ALWAYS);
            GridPane.setHgrow(errorText, Priority.NEVER);
            errorText.wrappingWidthProperty().bind(cameraAddress.widthProperty());
            gridContent.add(errorText, 0, 0);
            gridContent.add(cameraAddress, 0, 1);

            DialogPane dialogPane = getDialogPane();
            dialogPane.setContent(gridContent);
            dialogPane.setStyle(root.getStyle());
            dialogPane.getStylesheets().addAll(root.getStylesheets());
            dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            dialogPane.lookupButton(ButtonType.OK).setDisable(true);
        }

        public String getCameraAddress() {
            return cameraAddress.getText();
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
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("Image Files",
                            "*.bmp", "*.dib",           // Windows bitmaps
                            "*.jpeg", "*.jpg", "*.jpe", // JPEG files
                            "*.jp2",                    // JPEG 2000 files
                            "*.png",                    // Portable Network Graphics
                            "*.webp",                   // WebP
                            "*.pbm", "*.pgm", "*.ppm",  // Portable image format
                            "*.sr", "*.ras",            // Sun rasters
                            "*.tiff", "*.tif"           // TIFF files
                    ),
                    new ExtensionFilter("All Files", "*.*"));

            final List<File> imageFiles = fileChooser.showOpenMultipleDialog(this.getScene().getWindow());

            if (imageFiles == null) return;

            // Add a new source for each image .
            if (imageFiles.size() == 1) {
                try {
                    final ImageFileSource imageFileSource = imageSourceFactory.create(imageFiles.get(0));
                    imageFileSource.initialize();
                    eventBus.post(new SourceAddedEvent(imageFileSource));
                } catch (IOException e) {
                    eventBus.post(new UnexpectedThrowableEvent(e, "The image selected was invalid"));
                }
            } else {
                try {
                    final MultiImageFileSource multiImageFileSource = multiImageSourceFactory.create(imageFiles);
                    multiImageFileSource.initialize();
                    eventBus.post(new SourceAddedEvent(multiImageFileSource));
                } catch (IOException e) {
                    eventBus.post(new UnexpectedThrowableEvent(e, "One of the images selected was invalid"));
                }
            }
        });

        webcamButton = addButton("Add\nWebcam", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            final Parent root = this.getScene().getRoot();

            // Show a dialog for the user to pick a camera index
            final WebCamSourceDialog dialog = new WebCamSourceDialog(root);

            // If the user clicks OK, add a new camera source
            loadCamera(dialog,
                    () -> {
                        int cameraIndex = dialog.getCameraIndex();
                        double exposure = dialog.getExposure();
                        CameraSource cameraSource = cameraSourceFactory.create(cameraIndex, exposure);
                        cameraSource.initialize();
                        return cameraSource;
                    },
                    e -> {
                        dialog.errorText.setText(e.getMessage());
                    });
        });

        ipcamButton = addButton("Add IP\nCamera", getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
            final Parent root = this.getScene().getRoot();

            // Show a dialog for the user to pick a camera URL

            final IPCamSourceSourceDialog dialog = new IPCamSourceSourceDialog(root);

            // If the user clicks OK, add a new camera source
            loadCamera(dialog,
                    () -> {
                        final CameraSource cameraSource = cameraSourceFactory.create(dialog.getCameraAddress());
                        cameraSource.initialize();
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
        activeDialog = Optional.of(dialog);
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
        activeDialog = Optional.empty();
    }


    /**
     * Add a new button for adding a source.  This method takes care of setting the event handler.
     */
    private Button addButton(String text, URL graphicURL, EventHandler<? super MouseEvent> onMouseClicked) {
        final ImageView graphic = new ImageView(graphicURL.toString());
        graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
        graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

        final Button button = new Button(text, graphic);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setOnMouseClicked(onMouseClicked);

        this.getChildren().add(button);
        return button;
    }

    @VisibleForTesting
    Button getWebcamButton() {
        return webcamButton;
    }

    @VisibleForTesting
    Button getIpcamButton() {
        return ipcamButton;
    }

    @VisibleForTesting
    void closeDialogs() {
        activeDialog.ifPresent(dialog -> {
            for (ButtonType bt : dialog.getDialogPane().getButtonTypes()) {
                if (bt.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    Button cancelButton = (Button) dialog.getDialogPane().lookupButton(bt);
                    Platform.runLater(() -> cancelButton.fire());
                    break;
                }
            }
        });
    }
}
