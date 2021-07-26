package edu.wpi.grip.core;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public final class Loggers {

  private Loggers() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Sets up loggers to print to stdout and to ~/GRIP/GRIP.log. This should only be called once
   * in the application lifecycle, at startup.
   */
  public static void setupLoggers() {
    // Set up the global level logger. This handles IO for all loggers.
    final Logger globalLogger = LogManager.getLogManager().getLogger("");

    try {
      // Remove the default handlers that stream to System.err
      for (Handler handler : globalLogger.getHandlers()) {
        globalLogger.removeHandler(handler);
      }

      GripFileManager.GRIP_DIRECTORY.mkdirs();
      final Handler fileHandler
          = new FileHandler(GripFileManager.GRIP_DIRECTORY.getPath() + "/GRIP.log");

      //Set level to handler and logger
      fileHandler.setLevel(Level.INFO);
      globalLogger.setLevel(Level.INFO);

      // We need to stream to System.out instead of System.err
      final StreamHandler sh = new StreamHandler(System.out, new SimpleFormatter()) {

        @Override
        public synchronized void publish(final LogRecord record) {
          super.publish(record);
          // For some reason this doesn't happen automatically.
          // This will ensure we get all of the logs printed to the console immediately
          // when running on a remote device.
          flush();
        }
      };
      sh.setLevel(Level.CONFIG);

      globalLogger.addHandler(sh); // Add stream handler

      globalLogger.addHandler(fileHandler); //Add the handler to the global logger

      fileHandler.setFormatter(new SimpleFormatter()); //log in text, not xml

      globalLogger.config("Configuration done."); //Log that we are done setting up the logger
      globalLogger.config("GRIP Version: " + edu.wpi.grip.core.Main.class.getPackage()
          .getImplementationVersion());

    } catch (IOException exception) { //Something happened setting up file IO
      throw new IllegalStateException("Failed to configure the Logger", exception);
    }
  }

}
