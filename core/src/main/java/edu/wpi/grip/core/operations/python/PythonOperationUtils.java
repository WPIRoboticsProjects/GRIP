package edu.wpi.grip.core.operations.python;

import edu.wpi.grip.core.GripFileManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Utility class handling functionality for custom python operation files on disk.
 */
public final class PythonOperationUtils {

  /**
   * The directory where custom python operation files are stored.
   */
  public static final File DIRECTORY = new File(GripFileManager.GRIP_DIRECTORY, "operations");

  private PythonOperationUtils() {

  }

  /**
   * Reads the contents of the given file. Assumes it's encoded as UTF-8.
   *
   * @param file the file to read
   * @return the String contents of the file, in UTF-8 encoding
   */
  public static String read(File file) {
    if (!file.getParentFile().equals(DIRECTORY) || !file.getName().endsWith(".py")) {
      throw new IllegalArgumentException(
          "Not a custom python operation: " + file.getAbsolutePath());
    }
    try {
      return new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
    } catch (IOException e) {
      throw new RuntimeException("Could not read " + file.getAbsolutePath(), e);
    }
  }

  /**
   * Ensures that {@link #DIRECTORY} exists.
   */
  public static void checkDirExists() {
    if (DIRECTORY.exists()) {
      return;
    }
    DIRECTORY.mkdirs();
  }

}
