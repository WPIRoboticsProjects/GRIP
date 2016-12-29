package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.settings.CodeGenerationSettings;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dialog for code generation settings.
 */
public class CodeGenerationSettingsDialog extends Dialog<CodeGenerationSettings> {

  /**
   * Creates a new dialog to display code generation settings.
   *
   * @param root the root pane of the code generation settings scene. This <i>must</i> have the
   *             controller as the property "controller"
   */
  public CodeGenerationSettingsDialog(@Nonnull Pane root) {
    super();
    Object controllerObj = root.getProperties().get("controller");
    checkNotNull(controllerObj, "The root pane must have a 'controller' property");
    checkArgument(controllerObj instanceof CodeGenerationOptionsController,
        "Unexpected controller class: " + controllerObj.getClass().getName());
    CodeGenerationOptionsController controller = (CodeGenerationOptionsController) controllerObj;

    setTitle("Code Generation Settings");
    setHeaderText(null);
    setGraphic(null);
    getDialogPane().setContent(root);
    getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().styleProperty().bind(root.styleProperty());
    getDialogPane().getStylesheets().setAll(root.getStylesheets());
    root.requestFocus();
    setResultConverter(bt -> {
      if (ButtonBar.ButtonData.OK_DONE.equals(bt.getButtonData())) {
        return controller.getOptions();
      } else {
        return null;
      }
    });
  }

}
