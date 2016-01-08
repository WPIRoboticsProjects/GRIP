package edu.wpi.grip.ui;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.ProjectSettings;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.serialization.Project;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * The Controller for the application window.
 */
public class MainWindowController {

    @FXML
    private Parent root;
    @FXML
    private SplitPane topPane;
    @FXML
    private Region bottomPane;
    @FXML
    private Region pipelineView;
    @Inject
    private EventBus eventBus;
    @Inject
    private Pipeline pipeline;
    @Inject
    private Palette palette;
    @Inject
    private Project project;
    @Inject
    private DeployerController.Factory deployerControllerFactoy;

    public void initialize() {
        pipelineView.prefHeightProperty().bind(bottomPane.heightProperty());
    }

    /**
     * If there are any steps in the pipeline, give the user a chance to cancel an action or save the current project.
     *
     * @return true If the user has not chosen to
     */
    private boolean showConfirmationDialogAndWait() {
        if (!pipeline.getSteps().isEmpty()) {
            final ButtonType save = new ButtonType("Save");
            final ButtonType dontSave = ButtonType.NO;
            final ButtonType cancel = ButtonType.CANCEL;

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
                        saveProject();
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
        if (showConfirmationDialogAndWait()) {
            pipeline.clear();
            project.setFile(Optional.empty());
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
        if (showConfirmationDialogAndWait()) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Project");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("GRIP File", "*.grip"),
                    new ExtensionFilter("All Files", "*", "*.*"));

            project.getFile().ifPresent(file -> fileChooser.setInitialDirectory(file.getParentFile()));

            final File file = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) {
                project.open(file);
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
        if (project.getFile().isPresent()) {
            // Immediately save the project to whatever file it was loaded from or last saved to.
            project.save(project.getFile().get());
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
        fileChooser.getExtensionFilters().add(new ExtensionFilter("GRIP File", "*.grip"));

        project.getFile().ifPresent(file -> fileChooser.setInitialDirectory(file.getParentFile()));

        final File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return false;
        }

        project.save(file);
        return true;
    }

    @FXML
    public void showProjectSettingsEditor() {
        final ProjectSettings projectSettings = pipeline.getProjectSettings().clone();

        ProjectSettingsEditor projectSettingsEditor = new ProjectSettingsEditor(root, projectSettings);
        projectSettingsEditor.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                eventBus.post(new ProjectSettingsChangedEvent(projectSettings));
            }
        });
    }

    @FXML
    public void quit() {
        if (showConfirmationDialogAndWait()) {
            Platform.exit();
        }
    }

    @FXML
    public void deployFRC() {
        if (project.getFile().isPresent()) {
            final DeployerController deployerController = deployerControllerFactoy.create();
            deployerController.getRoot().getStylesheets().addAll(root.getStylesheets());
            deployerController.getRoot().setStyle(root.getStyle());
            final Dialog<ButtonType> dialog = new Dialog();
            dialog.setDialogPane(deployerController.getRoot());
            dialog.setResizable(true);
            dialog.showAndWait();
        } else {
            final Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "You must have saved your project before it can be deployed to a remote device.");
            alert.getDialogPane().getStylesheets().addAll(root.getStylesheets());
            alert.getDialogPane().setStyle(root.getStyle());

            // Workaround for JavaFX's default Alert styling causing text to get cut off
            Label alertLabel = (Label) alert.getDialogPane().getChildren().filtered(node -> node instanceof Label).get(0);
            alertLabel.setMinHeight(Region.USE_PREF_SIZE);

            alert.showAndWait();
        }

    }
}
