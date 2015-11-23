package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.events.SetSinkEvent;
import edu.wpi.grip.core.operations.composite.*;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.sinks.DummySink;
import edu.wpi.grip.generated.CVOperations;
import edu.wpi.grip.ui.pipeline.PipelineView;
import edu.wpi.grip.ui.preview.PreviewsView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * The Controller for the application window.
 */
public class MainWindowView extends VBox {

    @FXML
    private SplitPane topPane;

    @FXML
    private ScrollPane bottomPane;

    final private EventBus eventBus;

    final private PreviewsView previews;
    final private PaletteView palette;
    final private PipelineView pipeline;

    final private Project project;

    public MainWindowView(final EventBus eventBus) {
        eventBus.register(this);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("FXML Failed to load", e);
        }

        this.eventBus = eventBus;

        this.previews = new PreviewsView(eventBus);
        this.palette = new PaletteView(eventBus, new Palette(this.eventBus));
        this.pipeline = new PipelineView(eventBus, new Pipeline(this.eventBus));

        this.topPane.getItems().addAll(previews, palette);
        this.bottomPane.setContent(pipeline);
        pipeline.prefHeightProperty().bind(this.bottomPane.heightProperty());

        // Add the default built-in operations to the palette
        eventBus.post(new OperationAddedEvent(new BlurOperation()));
        eventBus.post(new OperationAddedEvent(new DesaturateOperation()));
        eventBus.post(new OperationAddedEvent(new RGBThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSVThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new HSLThresholdOperation()));
        eventBus.post(new OperationAddedEvent(new FindBlobsOperation()));
        eventBus.post(new OperationAddedEvent(new FindLinesOperation()));
        eventBus.post(new OperationAddedEvent(new FilterLinesOperation()));
        eventBus.post(new OperationAddedEvent(new MaskOperation()));
        eventBus.post(new OperationAddedEvent(new MinMaxLoc()));
        eventBus.post(new OperationAddedEvent(new NewPointOperation()));
        eventBus.post(new OperationAddedEvent(new NewSizeOperation()));
        eventBus.post(new OperationAddedEvent(new MatFieldAccessor()));

        // Add all of the auto-generated OpenCV operations
        CVOperations.addOperations(eventBus);

        eventBus.post(new SetSinkEvent(new DummySink()));

        this.project = new Project(this.eventBus, this.pipeline.getPipeline(), this.palette.getPalette());
    }

    /**
     * If there are any steps in the pipeline, give the user a chance to cancel an action or save the current project.
     *
     * @return true If the user has not chosen to
     */
    private boolean showConfirmationDialogAndWait() {
        if (!this.pipeline.getSteps().isEmpty()) {
            final ButtonType save = new ButtonType("Save");
            final ButtonType dontSave = ButtonType.NO;
            final ButtonType cancel = ButtonType.CANCEL;

            final Parent root = this.getScene().getRoot();
            final Dialog<ButtonType> dialog = new Dialog();
            dialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
            dialog.getDialogPane().setStyle(root.getStyle());
            dialog.setTitle("Save Project?");
            dialog.setHeaderText("Save the current project first?");
            dialog.getDialogPane().getButtonTypes().setAll(save, dontSave, cancel);

            if (dialog.showAndWait().isPresent()) {
                if (dialog.getResult().equals(cancel)) {
                    return false;
                }

                // If the user chose "Save", automatically show a save dialog and block until the user has had a
                // chance to save the project.
                if (dialog.getResult().equals(save)) {
                    try {
                        this.saveProject();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    /**
     * Delete everything in the current project.
     * <p>
     * If there are any steps in the pipeline, an "are you sure?" dialog is shown.
     */
    @FXML
    public void newProject() {
        if (this.showConfirmationDialogAndWait()) {
            this.pipeline.getPipeline().clear();
            this.project.setFile(Optional.empty());
        }
    }

    /**
     * Show a dialog for the user to pick a file to open a project from.
     * <p>
     * If there are any steps in the pipeline, an "are you sure?" dialog is shown. (TODO)
     *
     * @throws IOException
     */
    @FXML
    public void openProject() throws IOException {
        if (this.showConfirmationDialogAndWait()) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Project");

            this.project.getFile().ifPresent(file -> fileChooser.setInitialDirectory(file.getParentFile()));

            final File file = fileChooser.showOpenDialog(this.getScene().getWindow());
            if (file != null) {
                this.project.open(file);
            }
        }
    }

    /**
     * Immediately save the project to whatever file it was loaded from or previously saved to.  If there isn't such
     * a file, this is the same as {@link #saveProjectAs()}.
     *
     * @return true if the user does not cancel the save
     * @throws IOException
     */
    @FXML
    public boolean saveProject() throws IOException {
        if (this.project.getFile().isPresent()) {
            // Immediately save the project to whatever file it was loaded from or last saved to.
            this.project.save(this.project.getFile().get());
            return true;
        } else {
            return saveProjectAs();
        }
    }

    /**
     * Show a dialog that allows the user to save the current project to a file.  If the project was loaded from a
     * file or was previously saved to a file, the dialog should start out in the same directory.
     *
     * @return true if the user does not cancel the save
     * @throws IOException
     */
    @FXML
    public boolean saveProjectAs() throws IOException {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project As");

        this.project.getFile().ifPresent(file -> fileChooser.setInitialDirectory(file.getParentFile()));

        final File file = fileChooser.showOpenDialog(this.getScene().getWindow());
        if (file == null) {
            return false;
        }

        this.project.save(file);
        return true;
    }

    @FXML
    public void quit() {
        if (showConfirmationDialogAndWait()) {
            Platform.exit();
        }
    }
}

