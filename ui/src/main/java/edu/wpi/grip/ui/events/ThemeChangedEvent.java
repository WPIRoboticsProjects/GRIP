package edu.wpi.grip.ui.events;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event fired when the app theme changes.
 */
public class ThemeChangedEvent {

  private final String mainThemeFile;

  /**
   * Creates a new {@code ThemeChangedEvent}.
   *
   * @param mainThemeFile the main file of the theme
   */
  public ThemeChangedEvent(String mainThemeFile) {
    this.mainThemeFile = checkNotNull(mainThemeFile);
  }

  /**
   * Gets the main css file of the theme that was loaded.
   */
  public String getMainThemeFile() {
    return mainThemeFile;
  }

}
