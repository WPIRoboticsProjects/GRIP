package edu.wpi.grip.core.util;

import java.util.Locale;

public enum OperatingSystem {
  WINDOWS,
  MAC,
  LINUX;

  /**
   * Gets the operating system for the given OS name, as provided by
   * {@code System.getProperty("os.name")}.
   *
   * @param osName the name of the OS
   * @return the operating system for the given OS name
   */
  public static OperatingSystem forOsName(String osName) {
    String lower = osName.toLowerCase(Locale.US);
    if (lower.contains("win")) {
      return WINDOWS;
    } else if (lower.contains("mac")) {
      return MAC;
    } else {
      // Not strictly true, but since GRIP can never run on a different OS,
      // this is a safe general case
      return LINUX;
    }
  }
}
