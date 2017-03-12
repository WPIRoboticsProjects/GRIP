package edu.wpi.grip.ui.theme;

import edu.wpi.grip.ui.events.ThemeChangedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Comparator;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
          if (t.getLocation().toURI().toString().equals(preferences.get("theme", ""))) {
            b.setSelected(true);
          }
          b.setOnAction(event -> {
            eventBus.post(new ThemeChangedEvent(t.getLocation().toURI().toString()));
          });
          root.getChildren().add(b);
        });
  }

}
