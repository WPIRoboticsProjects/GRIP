package edu.wpi.grip.core;

import edu.wpi.grip.core.exception.IncompatibleVersionException;

import com.github.zafarkhaja.semver.Version;

/**
 * Manager for GRIP versions.
 */
public final class VersionManager {

  /**
   * The final release of GRIP without versioned saves.
   */
  public static final Version LAST_UNVERSIONED_RELEASE = Version.valueOf("1.5.0");

  /**
   * The current version of GRIP.
   */
  public static final Version CURRENT_VERSION = Version.valueOf("2.0.0-beta");

  /**
   * Checks compatibility between two versions of GRIP.
   *
   * @param current the current version of GRIP
   * @param check   the version to check
   *
   * @throws IncompatibleVersionException if the versions are incompatible
   */
  public static void checkVersionCompatibility(Version current, Version check)
      throws IncompatibleVersionException {
    if (check.getMajorVersion() > current.getMajorVersion()
        || check.getMinorVersion() > current.getMinorVersion()) {
      throw new IncompatibleVersionException("Incompatible future version: " + check);
    }
  }

}
