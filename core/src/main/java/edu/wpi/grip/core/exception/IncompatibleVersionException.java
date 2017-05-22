package edu.wpi.grip.core.exception;

import com.github.zafarkhaja.semver.Version;

/**
 * An exception thrown when trying to load a saved project created in an incompatible version
 * of GRIP.
 */
public class IncompatibleVersionException extends InvalidSaveException {

  private final Version loaded;

  public IncompatibleVersionException(Version loaded) {
    super("Incompatible future version: " + loaded);
    this.loaded = loaded;
  }

  public Version getLoaded() {
    return loaded;
  }

}
