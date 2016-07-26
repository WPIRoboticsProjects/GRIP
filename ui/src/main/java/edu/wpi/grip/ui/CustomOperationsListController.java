package edu.wpi.grip.ui;

import edu.wpi.grip.core.GripFileManager;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.python.PythonScriptFile;
import edu.wpi.grip.core.operations.python.PythonScriptOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.ui.annotations.ParametrizedController;

import com.google.common.eventbus.EventBus;
import com.google.common.io.Files;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;

/**
 * Controller for the custom operations list.
 */
@ParametrizedController(url = "CustomOperationsList.fxml")
public class CustomOperationsListController extends OperationListController {

  @Inject private EventBus eventBus;
  @Inject private InputSocket.Factory isf;
  @Inject private OutputSocket.Factory osf;

  @Override
  protected void initialize() {
    super.initialize();
  }

  @FXML
  private void createNewPythonOperation(ActionEvent actionEvent) {
    Dialog<String> dialog = new TextInputDialog();
    dialog.getDialogPane().setContent(new TextArea(PythonScriptFile.TEMPLATE));
    dialog.setResultConverter(bt -> {
      if (bt == ButtonType.OK) {
        return ((TextArea) dialog.getDialogPane().getContent()).getText();
      }
      return null;
    });
    Optional<String> result = dialog.showAndWait();
    if (result.isPresent()) {
      String code = result.get();
      String[] lines = code.split("\n");
      String name = null;
      // Find the name in the user code
      final Pattern p = Pattern.compile("name *= *\"(.*)\" *");
      for (String line : lines) {
        Matcher m = p.matcher(line);
        if (m.matches()) {
          name = m.group(1);
          break;
        }
      }
      if (name == null) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText("A name must be specified");
        a.showAndWait();
        return;
      } else if (name.isEmpty() || name.matches("[ \t]+")) {
        // Empty names are not allowed
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText("Name cannot be empty");
        a.showAndWait();
        return;
      } else if (!name.matches("[a-zA-Z0-9_\\- ]+")) {
        // Name can only contain (English) letters, numbers, underscores, dashes, and spaces
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText("Name contains illegal characters");
        a.showAndWait();
        return;
      }
      File file = new File(GripFileManager.GRIP_DIRECTORY + File.separator + "operations",
          name.replaceAll("[\\s]", "_") + ".py");
      if (file.exists()) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText("A file for the custom operation \"" + name + "\" already exists");
        a.showAndWait();
        return;
      }
      try {
        Files.write(code, file, Charset.defaultCharset());
      } catch (IOException e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText("Could not save custom operation to " + file.getAbsolutePath());
        a.showAndWait();
        return;
      }
      PythonScriptFile pcs = PythonScriptFile.create(code);
      eventBus.post(new OperationAddedEvent(new OperationMetaData(
          PythonScriptOperation.descriptionFor(pcs),
          () -> new PythonScriptOperation(isf, osf, pcs)
      )));
    }
  }

}
