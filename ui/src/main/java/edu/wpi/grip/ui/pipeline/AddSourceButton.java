package edu.wpi.grip.ui.pipeline;

import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.http.GripServer;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.HttpSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.core.sources.MultiImageFileSource;
import edu.wpi.grip.ui.util.DPIUtility;
import edu.wpi.grip.ui.util.SupplierWithIO;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.net.InetAddresses;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * A box of buttons that let the user add different kinds of {@link edu.wpi.grip.core.Source
 * Source}s.  Depending on which button is pressed, a different dialog is presented for the user to
 * construct that source.  As an example, the image file source results in a file picker that the
 * user can use to browse for an image.
 */
public class AddSourceButton extends MenuButton {

  @VisibleForTesting
  static final String SOURCE_DIALOG_STYLE_CLASS = "source-dialog";
  private final EventBus eventBus;

  private final MenuItem webcamButton;
  private final MenuItem ipcamButton;
  private final MenuItem httpButton;
  private Optional<Dialog> activeDialog = Optional.empty();

  @Inject
  AddSourceButton(EventBus eventBus,
                MultiImageFileSource.Factory multiImageSourceFactory,
                ImageFileSource.Factory imageSourceFactory,
                CameraSource.Factory cameraSourceFactory,
                HttpSource.Factory httpSourceFactory) {
    super("Add Source");
    this.eventBus = eventBus;
    
    addMenuItem("Image(s)",
        getClass().getResource("/edu/wpi/grip/ui/icons/add-image.png"), mouseEvent -> {
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

          final List<File> imageFiles = fileChooser.showOpenMultipleDialog(this.getScene()
              .getWindow());

          if (imageFiles == null) {
            return;
          }

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
              final MultiImageFileSource multiImageFileSource = multiImageSourceFactory
                  .create(imageFiles);
              multiImageFileSource.initialize();
              eventBus.post(new SourceAddedEvent(multiImageFileSource));
            } catch (IOException e) {
              eventBus.post(new UnexpectedThrowableEvent(e, "One of the images selected was "
                  + "invalid"));
            }
          }
        });

    webcamButton = addMenuItem("Webcam",
        getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
          final Parent root = this.getScene().getRoot();

          // Show a dialog for the user to pick a camera index
          final Spinner<Integer> cameraIndex = new Spinner<>(0, Integer.MAX_VALUE, 0);
          final SourceDialog dialog = new SourceDialog(root, cameraIndex);

          dialog.setTitle("Add Webcam");
          dialog.setHeaderText("Choose a camera");
          dialog.setContentText("index");

          dialog.getDialogPane().lookupButton(ButtonType.OK).requestFocus();

          // If the user clicks OK, add a new camera source
          loadCamera(dialog,
              () -> {
                final CameraSource cameraSource =
                    cameraSourceFactory.create(cameraIndex.getValue());
                cameraSource.initialize();
                return cameraSource;
              },
              e -> dialog.errorText.setText(e.getMessage()));
        });

    ipcamButton = addMenuItem("IP Camera",
        getClass().getResource("/edu/wpi/grip/ui/icons/add-webcam.png"), mouseEvent -> {
          final Parent root = this.getScene().getRoot();

          // Show a dialog for the user to pick a camera URL

          final TextField cameraAddress = new TextField();
          final SourceDialog dialog = new SourceDialog(root, cameraAddress);
          cameraAddress.setPromptText("Ex: http://10.1.90.11/mjpg/video.mjpg");
          cameraAddress.textProperty().addListener(observable -> {
            boolean validURL = true;

            try {
              new URL(cameraAddress.getText()).toURI();
            } catch (MalformedURLException e) {
              validURL = false;
              if (InetAddresses.isInetAddress(cameraAddress.getText()) || cameraAddress.getText()
                  .endsWith(".local")) {
                cameraAddress.setText("http://" + cameraAddress.getText());
                validURL = true;
              }
            } catch (URISyntaxException e) {
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
          loadCamera(
              dialog,
              () -> {
                final CameraSource cameraSource = cameraSourceFactory.create(cameraAddress
                    .getText());
                cameraSource.initialize();
                return cameraSource;
              },
              e -> dialog.errorText.setText(e.getMessage()));
        });

    httpButton = addMenuItem("HTTP",
        getClass().getResource("/edu/wpi/grip/ui/icons/publish.png"), mouseEvent -> {
          final Parent root = this.getScene().getRoot();
          // Show a dialog to pick the server path images will be uploaded on
          final String imageRoot = GripServer.IMAGE_UPLOAD_PATH + "/";
          final TextField serverPath = new TextField(imageRoot);
          final SourceDialog dialog = new SourceDialog(root, serverPath);
          serverPath.setPromptText("Ex: /GRIP/upload/image/foo");
          serverPath.textProperty().addListener(o -> {
            boolean valid = true;
            String text = serverPath.getText();
            valid = text.startsWith(imageRoot) && text.length() > imageRoot.length();
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(!valid);
          });

          dialog.setTitle("Choose path");
          dialog.setHeaderText("Enter the image upload path");
          dialog.showAndWait()
              .filter(ButtonType.OK::equals)
              .ifPresent(bt -> {
                final HttpSource httpSource = httpSourceFactory.create(serverPath.getText());
                httpSource.initialize();
                eventBus.post(new SourceAddedEvent(httpSource));
              });
        });

  }

  /**
   * @param dialog               The dialog to load the camera with.
   * @param cameraSourceSupplier The supplier that will create the camera.
   * @param failureCallback      The handler for when the camera source supplier throws an IO
   *                             Exception.
   */
  private void loadCamera(Dialog<ButtonType> dialog, SupplierWithIO<CameraSource>
      cameraSourceSupplier, Consumer<IOException> failureCallback) {
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
  private MenuItem addMenuItem(String text, URL graphicURL, EventHandler<ActionEvent>
      onActionEvent) {
    final ImageView graphic = new ImageView(graphicURL.toString());
    graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
    graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

    final MenuItem menuItem = new MenuItem("  " + text, graphic);
    menuItem.setOnAction(onActionEvent);

    getItems().add(menuItem);
    return menuItem;
  }

  @VisibleForTesting
  MenuItem getWebcamButton() {
    return webcamButton;
  }

  @VisibleForTesting
  MenuItem getIpcamButton() {
    return ipcamButton;
  }

  @VisibleForTesting
  MenuItem getHttpButton() {
    return httpButton;
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

  public interface Factory {
    AddSourceButton create();
  }

  private static class SourceDialog extends Dialog<ButtonType> {
    private final Text errorText = new Text();

    private SourceDialog(final Parent root, Control inputField) {
      super();

      this.getDialogPane().getStyleClass().add(SOURCE_DIALOG_STYLE_CLASS);

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
}
