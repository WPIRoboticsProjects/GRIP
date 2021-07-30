package edu.wpi.grip.preloader;

import javafx.application.Application;

public final class Launch {

  private Launch() {
  }

  /**
   * Main entry point for launching GRIP. We use an explicit main method in a separate class to
   * allow the JavaFX application to be launched without needing to go through the JVM's module
   * reflection (which fails when JavaFX is not on the module path - i.e. ALWAYS).
   */
  public static void main(String[] args) {
    // JavaFX 11+ uses GTK3 by default, and has problems on some display servers
    // This flag forces JavaFX to use GTK2
    System.setProperty("jdk.gtk.version", "2");
    Application.launch(GripPreloader.class, args);
  }
}
