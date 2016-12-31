package edu.wpi.grip.ui;

import edu.wpi.grip.core.settings.AppSettings;
import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.ui.util.DPIUtility;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * A JavaFX dialog that lets the user edit the project {@link ProjectSettings}.
 */
public class ProjectSettingsEditor extends Dialog<ButtonType> {

  private static class CustomPropertySheet extends PropertySheet {
    public CustomPropertySheet(ObservableList<Item> items) {
      super(items);
      setMode(Mode.NAME);
      setModeSwitcherVisible(false);
      setSearchBoxVisible(false);
    }
  }

  @SuppressWarnings("JavadocMethod")
  public ProjectSettingsEditor(Parent root,
                               ProjectSettings projectSettings,
                               AppSettings appSettings) {
    super();

    VBox content = new VBox(
        new CustomPropertySheet(BeanPropertyUtils.getProperties(projectSettings)),
        new Separator(),
        new CustomPropertySheet(BeanPropertyUtils.getProperties(appSettings))
    );
    content.setSpacing(5.0);

    DialogPane pane = getDialogPane();
    pane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    pane.setContent(content);
    pane.styleProperty().bind(root.styleProperty());
    pane.getStylesheets().addAll(root.getStylesheets());
    pane.setPrefSize(DPIUtility.SETTINGS_DIALOG_SIZE, DPIUtility.SETTINGS_DIALOG_SIZE);

    ImageView graphic = new ImageView(
        new Image(getClass().getResourceAsStream("icons/settings.png")));
    graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
    graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

    setTitle("Settings");
    setHeaderText("Settings");
    setGraphic(graphic);
    setResizable(true);
  }
}
