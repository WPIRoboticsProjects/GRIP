package edu.wpi.grip.ui;

import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.python.PythonScriptFile;
import edu.wpi.grip.core.operations.python.PythonScriptOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.annotations.ParametrizedController;
import edu.wpi.grip.ui.python.PythonEditorController;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controller for the custom operations list.
 */
@ParametrizedController(url = "CustomOperationsList.fxml")
public class CustomOperationsListController extends OperationListController {

  @FXML private Button createNewButton;
  @Inject private EventBus eventBus;
  @Inject private InputSocket.Factory isf;
  @Inject private OutputSocket.Factory osf;
  @Inject private Palette palette;
  @Inject private Main main;
  @Inject private Pipeline pipeline;

  @FXML
  @SuppressWarnings("PMD.UnusedPrivateMethod")
  private void createNewPythonOperation() {
    PythonEditorController editorController = loadEditor();
    editorController.injectMembers(
        name -> palette.getOperationByName(name).isPresent()
            || pipeline.getSteps()
            .stream()
            .map(step -> step.getOperationDescription().name())
            .anyMatch(name::equals),
        main.getHostServices()
    );
    Stage stage = new Stage();
    stage.setTitle("Python Script Editor");
    stage.setScene(new Scene(editorController.getRoot()));
    createNewButton.setDisable(true);
    try {
      stage.showAndWait();
      String code = editorController.getScript();
      if (code != null) {
        PythonScriptFile script = PythonScriptFile.create(code);
        eventBus.post(new OperationAddedEvent(new OperationMetaData(
            PythonScriptOperation.descriptionFor(script),
            () -> new PythonScriptOperation(isf, osf, script)
        )));
      }
    } finally {
      // make sure the button is re-enabled even if an exception gets thrown
      createNewButton.setDisable(false);
    }
  }

  private PythonEditorController loadEditor() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(getClass().getResource("/edu/wpi/grip/ui/python/PythonEditor.fxml"));
    try {
      loader.load();
      return loader.getController();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't load python editor", e);
    }
  }

}
