package edu.wpi.grip.ui;

import edu.wpi.grip.preloader.GripPreloader;

import com.sun.javafx.application.LauncherImpl;

public final class Launch {

  private Launch() {
  }

  /**
   * Main entry point for launching GRIP. We use an explicit main method in a separate class to
   * allow the JavaFX application to be launched without needing to go through the JVM's
   * module reflection (which fails when JavaFX is not on the module path - i.e. ALWAYS).
   *
   * <p>This also allows us to specify GTK2 on Linux systems, since JavaFX defaults to GTK3 and
   * is thus broken on most distros.
   */
  public static void main(String[] args) {
    // JavaFX 11+ uses GTK3 by default, and has problems on some display servers
    // This flag forces JavaFX to use GTK2
    System.setProperty("jdk.gtk.version", "2");
    LauncherImpl.launchApplication(Main.class, GripPreloader.class, args);
  }
}
