package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.events.ProjectSettingsChangedEvent;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.core.settings.SettingsProvider;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

/**
 * Controller for the code generation options pane.
 */
public class CodeGenerationOptionsController {

  @Inject
  private SettingsProvider settingsProvider;
  @Inject
  private EventBus eventBus;
  @Inject
  private Pipeline pipeline;

  @FXML
  private Pane root;
  @FXML
  private GridPane optionsGrid;
  @FXML
  private ComboBox<Language> languageSelector;
  @FXML
  private CheckBox implementVisionPipeline;
  @FXML
  private TextField classNameField;
  @FXML
  private Label saveLocationField;
  @FXML
  private Button browseButton;
  @FXML
  private StackPane extrasPane;
  @FXML
  private Pane javaControls;
  @FXML
  private TextField packageNameField;
  @FXML
  private Pane pythonControls;
  @FXML
  private TextField moduleNameField;

  private Language language;
  private boolean canImplementPipeline = true;

  @FXML
  private void initialize() {
    languageSelector.setItems(FXCollections.observableArrayList(Language.values()));
    extrasPane.setVisible(false);
    root.getProperties().put("controller", this);
    updateImplementButton();
  }

  private void updateImplementButton() {
    // Use runLater because pipeline change events are fired before the pipeline actually updates
    Platform.runLater(() -> {
      canImplementPipeline = canImplementVisionPipeline();
      implementVisionPipeline.setDisable(!canImplementPipeline);
      if (!canImplementPipeline) {
        implementVisionPipeline.setSelected(false);
      }
    });
  }

  private boolean canImplementVisionPipeline() {
    // - Currently, only Java and C++ support this
    // - Only a single source of type Mat is allowed
    // - No non-image inputs may be used (OK if they're not connected)
    boolean supportedLanguage = language != Language.PYTHON;
    boolean onlyOneImageInput = pipeline.getSources().stream()
        .flatMap(s -> s.getOutputSockets().stream())
        .filter(s -> Mat.class.equals(s.getSocketHint().getType()))
        .filter(s -> !s.getConnections().isEmpty())
        .count() == 1;
    boolean noConnectedNonImageInputs = pipeline.getSources().stream()
        .flatMap(s -> s.getOutputSockets().stream())
        .filter(s -> !Mat.class.equals(s.getSocketHint().getType()))
        .filter(s -> !s.getConnections().isEmpty())
        .count() == 0;
    return supportedLanguage && onlyOneImageInput && noConnectedNonImageInputs;
  }

  @FXML
  private void setLanguage() {
    this.language = languageSelector.getSelectionModel().getSelectedItem();
    saveLocationField.setDisable(false);
    browseButton.setDisable(false);
    updateImplementButton();
    switch (language) {
      case JAVA:
        loadJavaControls();
        break;
      case CPP:
        loadCppControls();
        break;
      case PYTHON:
        loadPythonControls();
        break;
      default:
        throw new AssertionError(
            "Unknown language: " + languageSelector.getSelectionModel().getSelectedItem());
    }
  }

  /**
   * Loads Java-specific controls into the pane.
   */
  private void loadJavaControls() {
    extrasPane.setVisible(true);
    javaControls.setVisible(true);
    pythonControls.setVisible(false);
    javaControls.toFront();
  }

  /**
   * Loads C++ specific controls into the pane.
   */
  private void loadCppControls() {
    extrasPane.setVisible(false);
  }

  /**
   * Loads Python-specific controls into the pane.
   */
  private void loadPythonControls() {
    extrasPane.setVisible(true);
    javaControls.setVisible(false);
    pythonControls.setVisible(true);
    pythonControls.toFront();
  }

  @FXML
  private void browseForSave() {
    DirectoryChooser dc = new DirectoryChooser();
    File destDir = settingsProvider.getProjectSettings().getCodegenDestDir();
    if (destDir == null) {
      destDir = new File(System.getProperty("user.home")).getAbsoluteFile();
    }
    dc.setInitialDirectory(destDir);
    dc.setTitle("Choose save location");
    File save = dc.showDialog(optionsGrid.getScene().getWindow());
    if (save == null) {
      return;
    }
    saveLocationField.setText(save.getAbsolutePath());
  }

  /**
   * Gets the code generation options specified in the UI.
   */
  public CodeGenerationOptions getOptions() {
    return CodeGenerationOptions.builder()
        .language(language)
        .className(classNameField.getText())
        .implementVisionPipeline(implementVisionPipeline.isSelected())
        .saveDir(saveLocationField.getText())
        .packageName(packageNameField.getText())
        .moduleName(moduleNameField.getText())
        .build();
  }

  @Subscribe
  public void onProjectSettingsChanged(ProjectSettingsChangedEvent event) {
    Platform.runLater(() -> {
      final ProjectSettings settings = event.getProjectSettings();
      saveLocationField.setText(settings.getCodegenDestDir().getAbsolutePath());
      classNameField.setText(settings.getGeneratedPipelineName());
      packageNameField.setText(settings.getGeneratedJavaPackage());
      moduleNameField.setText(settings.getGeneratedPythonModuleName());
      Language language = Language.get(settings.getPreferredGeneratedLanguage());
      if (language != null) {
        languageSelector.getSelectionModel().select(language);
        setLanguage();
      }
    });
  }

  @Subscribe
  public void onConnectionAdded(ConnectionAddedEvent e) {
    if (e.getConnection().getOutputSocket().getSource().isPresent()) {
      updateImplementButton();
    }
  }

  @Subscribe
  public void onConnectionRemoved(ConnectionRemovedEvent e) {
    if (e.getConnection().getOutputSocket().getSource().isPresent()) {
      updateImplementButton();
    }
  }

}
