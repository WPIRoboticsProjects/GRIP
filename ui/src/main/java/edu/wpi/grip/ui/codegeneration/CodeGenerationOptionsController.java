package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.CodeGenerationSettingsChangedEvent;
import edu.wpi.grip.core.events.ConnectionAddedEvent;
import edu.wpi.grip.core.events.ConnectionRemovedEvent;
import edu.wpi.grip.core.settings.CodeGenerationSettings;
import edu.wpi.grip.core.settings.SettingsProvider;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;

/**
 * Controller for the code generation options pane.
 */
public class CodeGenerationOptionsController {

  private static final Logger logger =
      Logger.getLogger(CodeGenerationOptionsController.class.getName());

  @Inject
  private SettingsProvider settingsProvider;
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
  private Label saveLocationLabel;
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

  private static final String CLASS_NAME_REGEX = "^|([A-Z][a-z]*)+$";
  private static final String PACKAGE_REGEX = "^|([a-z]+[a-z0-9]*\\.?)+$";
  private static final String MODULE_REGEX = "^|([a-z]+_?)+$";

  @FXML
  private void initialize() {
    languageSelector.setItems(FXCollections.observableArrayList(Language.values()));
    saveLocationLabel.setTooltip(new Tooltip());
    saveLocationLabel.textProperty().addListener((obs, oldValue, newValue) -> {
      saveLocationLabel.getTooltip().setText(newValue);
    });
    extrasPane.setVisible(false);
    root.getProperties().put("controller", this);
    updateImplementButton();
    setTextFilter(classNameField, CLASS_NAME_REGEX);
    setTextFilter(packageNameField, PACKAGE_REGEX);
    setTextFilter(moduleNameField, MODULE_REGEX);
    loadSettings(CodeGenerationSettings.DEFAULT_SETTINGS); // load the default settings
  }

  private void loadSettings(CodeGenerationSettings settings) {
    saveLocationLabel.setText(settings.getSaveDir());
    classNameField.setText(settings.getClassName());
    packageNameField.setText(settings.getPackageName());
    moduleNameField.setText(settings.getModuleName());
    implementVisionPipeline.setSelected(settings.shouldImplementWpilibPipeline());
    Language language = Language.get(settings.getLanguage());
    if (language != null) {
      languageSelector.getSelectionModel().select(language);
      setLanguage();
    }
  }

  private static void setTextFilter(TextField f, String regex) {
    f.textProperty().addListener((obs, oldValue, newValue) -> {
      if (!newValue.matches(regex)) {
        f.setText(oldValue);
      }
    });
  }

  private void updateImplementButton() {
    // Use runLater because pipeline change events are fired before the pipeline actually updates
    Platform.runLater(() -> {
      boolean canImplementPipeline = canImplementVisionPipeline();
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
    saveLocationLabel.setDisable(false);
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
    File destDir = new File(settingsProvider.getCodeGenerationSettings().getSaveDir());
    if (!Files.isDirectory(destDir.toPath())) {
      logger.warning("Loaded save directory does not exist, setting to default.");
      destDir = new File(CodeGenerationSettings.DEFAULT_SETTINGS.getSaveDir());
    }
    dc.setInitialDirectory(destDir);
    dc.setTitle("Choose save location");
    File save = dc.showDialog(optionsGrid.getScene().getWindow());
    if (save == null) {
      return;
    }
    saveLocationLabel.setText(save.getAbsolutePath());
  }

  /**
   * Gets the code generation options specified in the UI.
   */
  public CodeGenerationSettings getOptions() {
    return CodeGenerationSettings.builder()
        .language(language.name)
        .className(classNameField.getText())
        .implementVisionPipeline(implementVisionPipeline.isSelected())
        .saveDir(saveLocationLabel.getText())
        .packageName(packageNameField.getText())
        .moduleName(moduleNameField.getText())
        .build();
  }

  @Subscribe
  public void onProjectSettingsChanged(CodeGenerationSettingsChangedEvent event) {
    Platform.runLater(() -> loadSettings(event.getCodeGenerationSettings()));
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
