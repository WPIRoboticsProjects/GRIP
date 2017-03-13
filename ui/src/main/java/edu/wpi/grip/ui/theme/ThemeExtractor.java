package edu.wpi.grip.ui.theme;

import com.google.inject.Singleton;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extracts themes from the app to ~/GRIP/themes/.
 */
@Singleton
public class ThemeExtractor {

  private static final Logger logger = Logger.getLogger(ThemeExtractor.class.getName());

  private static final String rootDir = ThemeManager.DEFAULT_THEME_DIR.getAbsolutePath();

  /**
   * Extracts all builtin themes.
   */
  public void extractAll() {
    try {
      FileUtils.copyURLToFile(getClass().getResource(toInternalPath("dark/theme.css")),
          new File(rootDir, "dark/theme.css"));
      FileUtils.copyURLToFile(getClass().getResource(toInternalPath("dark/controls.css")),
          new File(rootDir, "dark/controls.css"));
      FileUtils.copyURLToFile(getClass().getResource(toInternalPath("light/theme.css")),
          new File(rootDir, "light/theme.css"));
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not extract themes", e);
    }
  }

  private static String toInternalPath(String themeName) {
    return "/edu/wpi/grip/ui/theme/" + themeName;
  }

}
