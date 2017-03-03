package edu.wpi.grip.ui.theme;

import edu.wpi.grip.core.GripFileManager;
import edu.wpi.grip.ui.events.ThemeChangedEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Controller for the theme picker. Themes should be stored in {@link #THEME_ROOT}. The main theme
 * file must be named {@code theme.css}; any ancillary style sheets may have any name.
 */
public class ThemeController {

  private static final Logger logger = Logger.getLogger(ThemeController.class.getName());

  /**
   * The root directory for where themes are stored on the file system.
   */
  public static final File THEME_ROOT = new File(GripFileManager.GRIP_DIRECTORY, "themes");

  @FXML
  private VBox root;
  @Inject
  private EventBus eventBus;
  @Inject @Named("AppPreferences")
  private Preferences preferences;

  private final ToggleGroup toggleGroup = new ToggleGroup();
  private final List<Theme> themes = new ArrayList<>();

  /**
   * Initializes the theme controller.
   */
  @FXML
  public void initialize() {
    THEME_ROOT.mkdirs();

    // Scan for themes
    scanForThemes();
    makeButtons();
  }

  /**
   * Scans the disk for themes in {@link #THEME_ROOT}.
   */
  private void scanForThemes() {
    logger.info("Scanning for themes");
    File[] files = THEME_ROOT.listFiles();
    if (files == null) {
      return;
    }
    for (File themeDir : files) {
      if (!themeDir.isDirectory()) {
        continue;
      }
      final String name = WordUtils.capitalizeFully(themeDir.getName());
      File[] themeFiles = themeDir.listFiles();
      if (themeFiles == null) {
        continue;
      }
      for (File themeFile : themeFiles) {
        if (themeFile.getName().equals("theme.css")) {
          // found the main theme file
          themes.add(new Theme(name, themeFile.getAbsoluteFile()));
        }
      }
    }
    logger.info("Found themes: " + themes);
  }

  private void makeButtons() {
    root.getChildren().clear();
    themes.stream()
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
