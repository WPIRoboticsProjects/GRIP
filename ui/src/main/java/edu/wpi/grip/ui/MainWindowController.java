package edu.wpi.grip.ui;

import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.PipelineRunner;
import edu.wpi.grip.core.events.AppSettingsChangedEvent;
import edu.wpi.grip.core.events.BenchmarkEvent;
import edu.wpi.grip.core.events.CodeGenerationSettingsChangedEvent;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.events.TimerEvent;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.events.WarningEvent;
import edu.wpi.grip.core.serialization.Project;
import edu.wpi.grip.core.settings.AppSettings;
import edu.wpi.grip.core.settings.CodeGenerationSettings;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.settings.SettingsProvider;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.util.SafeShutdown;
import edu.wpi.grip.core.util.service.SingleActionListener;
import edu.wpi.grip.ui.codegeneration.CodeGenerationSettingsDialog;
import edu.wpi.grip.ui.codegeneration.Exporter;
import edu.wpi.grip.ui.components.StartStoppableButton;
import edu.wpi.grip.ui.util.DPIUtility;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Service;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.inject.Inject;

/**
 * The Controller for the application window.
 */
public class MainWindowController {

  private static final Logger logger = Logger.getLogger(MainWindowController.class.getName());

  @FXML
  private Parent root;
  @FXML
  private SplitPane topPane;
  @FXML
  private Region bottomPane;
  @FXML
  private Region pipelineView;
  @FXML
  private Pane deployPane;
  @FXML
  private Pane aboutPane;
  @FXML
  private Pane analysisPane;
  @FXML
  private Pane codegenPane;
  @FXML
  private MenuItem analyzeMenuItem;
  @FXML
  private HBox statusBar;
  @FXML
  private Label statusLabel;
  @FXML
  private Label elapsedTimeLabel;
  @Inject
  private EventBus eventBus;
  @Inject
  private Pipeline pipeline;
  @Inject
  private SettingsProvider settingsProvider;
  @Inject
  private PipelineRunner pipelineRunner;
  @Inject
  private StartStoppableButton.Factory startStoppableButtonFactory;
  @Inject
  private Palette palette;
  @Inject
  private Project project;

  private Stage aboutDialogStage;
  private Stage analysisStage;

  @FXML
  protected void initialize() {
    pipelineView.prefHeightProperty().bind(bottomPane.heightProperty());
    statusBar.getChildren().add(0, startStoppableButtonFactory.create(pipelineRunner));
    pipelineRunner.addListener(new SingleActionListener(() -> {
      final Service.State state = pipelineRunner.state();
      final String stateMessage =
          state.equals(Service.State.TERMINATED)
              ? "disabled "
              : "enabled  ";
      statusLabel.setText("Pipeline " + stateMessage);
      analyzeMenuItem.setDisable(state.equals(Service.State.TERMINATED));
    }), Platform::runLater);
    Platform.runLater(() -> root.getScene().getWindow().setOnCloseRequest(e -> {
      if (!quit()) {
        // Asked to quit but cancelled, consume the event to avoid closing the window
        e.consume();
      }
    }));
  }

  /**
   * If there are any steps in the pipeline, give the user a chance to cancel an action or save the
   * current project.
   *
   * @return true If the user has not chosen to
   */
  private boolean showConfirmationDialogAndWait() {
    if (!pipeline.getSteps().isEmpty() && project.isSaveDirty()) {
      final ButtonType save = new ButtonType("Save");
      final ButtonType dontSave = ButtonType.NO;
      final ButtonType cancel = ButtonType.CANCEL;

      final Dialog<ButtonType> dialog = new Dialog<>();
      dialog.getDialogPane().getStylesheets().addAll(root.getStylesheets());
      dialog.getDialogPane().setStyle(root.getStyle());
      dialog.setTitle("Save Project?");
      dialog.setHeaderText("Save the current project first?");
      dialog.getDialogPane().getButtonTypes().setAll(save, dontSave, cancel);

      if (!dialog.showAndWait().isPresent()) {
        return false;
      } else if (dialog.getResult().equals(cancel)) {
        return false;
      } else if (dialog.getResult().equals(save)) {
        // If the user chose "Save", automatically show a save dialog and block until the user
        // has had a chance to save the project.
        try {
          return saveProject();
        } catch (IOException e) {
          logger.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
      }
    }
    return true;
  }

  /**
   * Delete everything in the current project. If there are any steps in the pipeline, an "are you
   * sure?" dialog is shown.
   */
  @FXML
  public void newProject() {
    if (showConfirmationDialogAndWait()) {
      Thread clearThread = new Thread(() -> {
        pipelineRunner.stopAndAwait();
        pipeline.clear();
        project.setFile(Optional.empty());
        pipelineRunner.startAsync();
      }, "Pipeline Clear Thread");
      clearThread.setDaemon(true);
      clearThread.start();
    }
  }

  /**
   * Show a dialog for the user to pick a file to open a project from. If there are any steps in the
   * pipeline, an "are you sure?" dialog is shown. (TODO)
   */
  @FXML
  public void openProject() {
    if (showConfirmationDialogAndWait()) {
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Project");
      fileChooser.getExtensionFilters().addAll(
          new ExtensionFilter("GRIP File", "*.grip"),
          new ExtensionFilter("All Files", "*", "*.*"));

      project.getFile().ifPresent(file -> fileChooser.setInitialDirectory(file.getParentFile()));

      final File file = fileChooser.showOpenDialog(root.getScene().getWindow());
      if (file != null) {
        Thread fileOpenThread = new Thread(() -> {
          try {
            project.open(file);
          } catch (IOException e) {
            eventBus.post(new UnexpectedThrowableEvent(e, "Failed to load save file"));
          }
        }, "Project Open Thread");
        fileOpenThread.setDaemon(true);
        fileOpenThread.start();
      }
    }
  }

  /**
   * Immediately save the project to whatever file it was loaded from or previously saved to.  If
   * there isn't such a file, this is the same as {@link #saveProjectAs()}.
   *
   * @return true if the user does not cancel the save
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
   * Show a dialog that allows the user to save the current project to a file.  If the project was
   * loaded from a file or was previously saved to a file, the dialog should start out in the same
   * directory.
   *
   * @return true if the user does not cancel the save
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
  protected void showProjectSettingsEditor() {
    final ProjectSettings projectSettings = settingsProvider.getProjectSettings().clone();
    final AppSettings appSettings = settingsProvider.getAppSettings().clone();

    ProjectSettingsEditor projectSettingsEditor
        = new ProjectSettingsEditor(root, projectSettings, appSettings);
    projectSettingsEditor.showAndWait().ifPresent(buttonType -> {
      if (buttonType == ButtonType.OK) {
        eventBus.post(new ProjectSettingsChangedEvent(projectSettings));
        eventBus.post(new AppSettingsChangedEvent(appSettings));
      }
    });
  }

  @FXML
  protected void showProjectAboutDialog() throws IOException {
    if (aboutDialogStage == null) {
      aboutDialogStage = new Stage();
      aboutDialogStage.setScene(new Scene(aboutPane));
      aboutDialogStage.initStyle(StageStyle.UTILITY);
      aboutDialogStage.focusedProperty().addListener((observable, oldvalue, newvalue) -> {
        if (oldvalue) {
          aboutDialogStage.hide();
        }
      });
    }
    aboutDialogStage.show();
  }

  @FXML
  protected boolean quit() {
    if (showConfirmationDialogAndWait()) {
      pipelineRunner.stopAsync();
      SafeShutdown.exit(0);
      return true;
    }
    return false;
  }

  /**
   * Controls the export button in the main menu. Opens a filechooser with language selection.
   * The user can select the language to export to, save location and file name.
   */
  @FXML
  protected void generate() {
    if (pipeline.getSources().isEmpty()) {
      // No sources
      eventBus.post(new WarningEvent("Cannot generate code",
          "There are no sources in the pipeline"));
      return;
    } else if (pipeline.getSteps().isEmpty()) {
      // Sources, but no steps
      eventBus.post(new WarningEvent("Cannot generate code",
          "There are no steps in the pipeline"));
      return;
    } else if (pipeline.getConnections().isEmpty()) {
      // Sources and steps, but no connections
      eventBus.post(new WarningEvent("Cannot generate code",
          "There are no connections in the pipeline"));
      return;
    } else if (pipeline.getSteps().stream()
        .flatMap(s -> s.getInputSockets().stream())
        .filter(s -> SocketHint.View.NONE.equals(s.getSocketHint().getView()))
        .anyMatch(s -> !s.getValue().isPresent())) {
      // Some sockets aren't connected
      StringBuilder sb = new StringBuilder("The following steps are missing inputs:\n\n"); //NOPMD
      pipeline.getSteps().stream()
          .filter(step -> step.getInputSockets().stream().anyMatch(s ->
              SocketHint.View.NONE.equals(s.getSocketHint().getView())
                  && !s.getValue().isPresent()))
          .map(s -> Pair.of(pipeline.indexOf(s) + 1, s.getOperationDescription().name()))
          .forEach(p -> {
            sb.append(" - ").append(p.getRight());
            sb.append(" (at position ").append(p.getLeft()).append(")\n");
          });
      eventBus.post(new WarningEvent("Cannot generate code", sb.toString()));
      return;
    }
    Dialog<CodeGenerationSettings> optionsDialog = new CodeGenerationSettingsDialog(codegenPane);
    optionsDialog.showAndWait().ifPresent(settings -> {
      eventBus.post(new CodeGenerationSettingsChangedEvent(settings));
      Exporter exporter = new Exporter(pipeline.getSteps(), settings);
      final Set<String> nonExportableSteps = exporter.getNonExportableStepNames();
      if (!nonExportableSteps.isEmpty()) {
        StringBuilder b = new StringBuilder(
            "The following steps do not support code generation:\n\n"
        );
        nonExportableSteps.stream()
            .sorted()
            .forEach(n -> b.append(" - ").append(n).append("\n"));
        eventBus.post(new WarningEvent("Cannot generate code", b.toString()));
        return;
      }
      Thread exportRunner = new Thread(exporter);
      exportRunner.setDaemon(true);
      exportRunner.start();
    });
  }

  @FXML
  protected void deploy() {
    eventBus.post(new WarningEvent(
        "Deploy has been deprecated",
        "The deploy tool has been deprecated and is no longer supported. "
            + "It will be removed in a future release.\n\n"
            + "Instead, use code generation to create a Java, C++, or Python class that handles all"
            + " the OpenCV code and can be easily integrated into a WPILib robot program."));

    ImageView graphic = new ImageView(new Image("/edu/wpi/grip/ui/icons/settings.png"));
    graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
    graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

    deployPane.requestFocus();

    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Deploy");
    dialog.setHeaderText("Deploy");
    dialog.setGraphic(graphic);
    dialog.getDialogPane().getButtonTypes().setAll(ButtonType.CLOSE);
    dialog.getDialogPane().styleProperty().bind(root.styleProperty());
    dialog.getDialogPane().getStylesheets().setAll(root.getStylesheets());
    dialog.getDialogPane().setContent(deployPane);
    dialog.setResizable(true);
    dialog.showAndWait();
  }

  @Subscribe
  public void onWarningEvent(WarningEvent e) {
    if (Platform.isFxApplicationThread()) {
      showWarningAlert(e);
    } else {
      Platform.runLater(() -> showWarningAlert(e));
    }
  }

  private void showWarningAlert(WarningEvent e) {
    Alert alert = new WarningAlert(e.getHeader(), e.getBody(), root.getScene().getWindow());
    alert.showAndWait();
  }

  @Subscribe
  @SuppressWarnings({"PMD.UnusedPrivateMethod", "PMD.UnusedFormalParameter"})
  private void runStopped(TimerEvent event) {
    if (event.getTarget() instanceof PipelineRunner) {
      Platform.runLater(() -> updateElapsedTimeLabel(event.getElapsedTime()));
    }
  }

  private void updateElapsedTimeLabel(long elapsed) {
    elapsedTimeLabel.setText(
        String.format("Ran in %.1f ms (%.1f fps)",
            elapsed / 1e3,
            elapsed != 0 ? (1e6 / elapsed) : Double.NaN));
  }

  @FXML
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void showAnalysis() {
    if (analysisStage == null) {
      analysisStage = new Stage();
      analysisStage.setScene(new Scene(analysisPane));
      analysisStage.initOwner(root.getScene().getWindow());
      analysisStage.setTitle("Pipeline Analysis");
      analysisStage.getIcons().add(new Image("/edu/wpi/grip/ui/icons/grip.png"));
      analysisStage.setOnCloseRequest(event -> eventBus.post(BenchmarkEvent.finished()));
    }
    analysisStage.showAndWait();
  }
}
