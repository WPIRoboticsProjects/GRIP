package edu.wpi.grip.ui.theme;

import java.io.File;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A theme has a name (eg "Vanilla") and a file containing the root stylesheet. This stylesheet
 * <i>must</i> be named {@code theme.css}. Ancillary stylesheets may have any name.
 */
public class Theme {

  private final String name;
  private final File location;

  public Theme(String name, File location) {
    this.name = requireNonNull(name);
    this.location = requireNonNull(location);
  }

  /**
   * Gets the name of this theme.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the location of the root {@code theme.css} stylesheet.
   */
  public File getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Theme theme = (Theme) o;

    if (name != null ? !name.equals(theme.name) : theme.name != null) {
      return false;
    }
    return location != null ? location.equals(theme.location) : theme.location == null;

  }

  @Override
  public int hashCode() {
    return Objects.hash(name, location);
  }

  @Override
  public String toString() {
    return String.format("Theme(name='%s', location=%s)", name, location);
  }

}
