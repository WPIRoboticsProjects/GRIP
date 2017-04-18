package edu.wpi.grip.ui.theme;

import edu.wpi.grip.ui.events.ThemeChangedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.File;
import java.util.Comparator;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller for the theme picker.
 */
public class ThemeController {

  @FXML
  private VBox root;
  @Inject
  private EventBus eventBus;
  @Inject @Named("AppPreferences")
  private Preferences preferences;
  @Inject
  private ThemeManager themeManager;

  private final ToggleGroup toggleGroup = new ToggleGroup();

  /**
   * Initializes the theme controller.
   */
  @FXML
  public void initialize() {
    themeManager.scanForThemes();
    makeButtons();
  }

  private void makeButtons() {
    root.getChildren().clear();
    themeManager.getThemes().stream()
        .sorted(Comparator.comparing(Theme::getName)) // sort alphabetically
        .forEach(t -> {
          RadioButton b = new RadioButton(t.getName());
          b.setToggleGroup(toggleGroup);
          if (absolutePath(t).equals(preferences.get("theme", ""))) {
            b.setSelected(true);
          }
          b.setOnAction(event -> eventBus.post(new ThemeChangedEvent(absolutePath(t))));
          root.getChildren().add(b);
          File previewFile = findPreviewFile(t);
          if (previewFile.exists()) {
            b.setGraphic(new ImageView(previewFile.toURI().toString()));
          }
        });
  }

  private static String absolutePath(Theme theme) {
    return theme.getLocation().toURI().toString();
  }

  private static File findPreviewFile(Theme theme) {
    return new File(theme.getLocation().getParentFile(), "preview.png");
  }

}
