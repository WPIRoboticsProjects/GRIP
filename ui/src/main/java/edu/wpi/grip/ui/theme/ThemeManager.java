package edu.wpi.grip.ui.theme;

import edu.wpi.grip.core.GripFileManager;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;

import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manages GRIP themes. The main .css theme file <i>must</i> be named {@code theme.css};
 * any ancillary .css files may have any name. Theme names are based on the name of the directory
 * containing the {@code theme.css} file. For example, if a theme is structured like
 * <pre>
 *   themes
 *     - <i>Foo</i>
 *       - theme.css
 *       - extras.css
 *       - ...
 * </pre>
 * the name of the theme presented to the user will be <i>Foo</i>. The capitalization is independent
 * of the capitalization of the directory on the file system, eg "FooBar" becomes "Foobar" and
 * "Foo bar" becomes "Foo Bar". More generally, every word will be capitalized as if it were a
 * proper noun.
 */
@Singleton
public class ThemeManager {

  private static final Logger logger = Logger.getLogger(ThemeManager.class.getName());

  /**
   * The default directory for where themes are stored on the file system.
   */
  public static final File DEFAULT_THEME_DIR = new File(GripFileManager.GRIP_DIRECTORY, "themes");
  private static final String MAIN_THEME_FILE_NAME = "theme.css";

  private final Set<Theme> themes = new HashSet<>();

  protected ThemeManager() {
    if (!DEFAULT_THEME_DIR.isDirectory()) {
      logger.info("Default theme dir is not a directory, deleting");
      DEFAULT_THEME_DIR.delete();
    }
    DEFAULT_THEME_DIR.mkdirs();
  }

  /**
   * Scans for themes in the default location ({@link #DEFAULT_THEME_DIR}). All themes discovered
   * from all invocations of this method can be accessed with {@link #getThemes()}.
   */
  public void scanForThemes() {
    logger.info("Scanning for themes");
    File[] files = DEFAULT_THEME_DIR.listFiles();
    if (files == null) {
      // IO error or DEFAULT_THEME_DIR is not a directory (!)
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
        if (themeFile.getName().equals(MAIN_THEME_FILE_NAME)) {
          // found the main theme file
          themes.add(new Theme(name, themeFile.getAbsoluteFile()));
        }
      }
    }
    logger.info("Found themes: " + themes);
  }

  /**
   * Gets a read-only copy of the discovered themes.
   */
  public List<Theme> getThemes() {
    // defensive copy
    return ImmutableList.copyOf(themes);
  }

}
