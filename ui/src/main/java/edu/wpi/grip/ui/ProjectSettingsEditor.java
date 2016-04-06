package edu.wpi.grip.ui;

import edu.wpi.grip.core.settings.ProjectSettings;
import edu.wpi.grip.ui.util.DPIUtility;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

/**
 * A JavaFX dialog that lets the user edit the project {@link ProjectSettings}
 */
public class ProjectSettingsEditor extends Dialog<ButtonType> {

    public ProjectSettingsEditor(Parent root, ProjectSettings projectSettings) {
        super();

        PropertySheet propertySheet = new PropertySheet(BeanPropertyUtils.getProperties(projectSettings));
        propertySheet.setMode(PropertySheet.Mode.NAME);
        propertySheet.setModeSwitcherVisible(false);
        propertySheet.setSearchBoxVisible(false);

        DialogPane pane = getDialogPane();
        pane.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setContent(propertySheet);
        pane.styleProperty().bind(root.styleProperty());
        pane.getStylesheets().addAll(root.getStylesheets());
        pane.setPrefSize(DPIUtility.SETTINGS_DIALOG_SIZE, DPIUtility.SETTINGS_DIALOG_SIZE);

        ImageView graphic = new ImageView(new Image(getClass().getResourceAsStream("icons/settings.png")));
        graphic.setFitWidth(DPIUtility.SMALL_ICON_SIZE);
        graphic.setFitHeight(DPIUtility.SMALL_ICON_SIZE);

        setTitle("Settings");
        setHeaderText("Settings");
        setGraphic(graphic);
        setResizable(true);
    }
}
